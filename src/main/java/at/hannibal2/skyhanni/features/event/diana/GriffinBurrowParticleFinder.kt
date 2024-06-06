package at.hannibal2.skyhanni.features.event.diana

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.diana.BurrowDetectEvent
import at.hannibal2.skyhanni.events.diana.BurrowDugEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.minecraft.click.BlockClickEvent
import at.hannibal2.skyhanni.events.minecraft.packet.ReceivePacketEvent
import at.hannibal2.skyhanni.events.utils.DebugDataCollectEvent
import at.hannibal2.skyhanni.features.event.diana.DianaAPI.isDianaSpade
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeLimitedSet
import at.hannibal2.skyhanni.utils.mc.McWorld.getBlockAt
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S2APacketParticles
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GriffinBurrowParticleFinder {

    private val config get() = SkyHanniMod.feature.event.diana

    private val recentlyDugParticleBurrows = TimeLimitedSet<LorenzVec>(1.minutes)
    private val burrows = mutableMapOf<LorenzVec, Burrow>()
    private var lastDugParticleBurrow: LorenzVec? = null

    // This exists to detect the unlucky timing when the user opens a burrow before it gets fully detected
    private var fakeBurrow: LorenzVec? = null

    @HandleEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Griffin Burrow Particle Finder")

        if (!DianaAPI.isDoingDiana()) {
            event.addIrrelevant("not doing diana")
            return
        }

        event.addData {
            add("burrows: ${burrows.size}")
            for (burrow in burrows.values) {
                val location = burrow.location
                val found = burrow.found
                add(location.printWithAccuracy(1))
                add(" type: " + burrow.getType())
                add(" found: $found")
                add(" ")
            }
        }
    }

    @HandleEvent(priority = HandleEvent.LOW, receiveCancelled = true)
    fun onPacketReceive(event: ReceivePacketEvent) {
        if (!isEnabled()) return
        if (!config.burrowsSoopyGuess) return
        val packet = event.packet

        if (packet is S2APacketParticles) {

            val particleType = ParticleType.getParticleType(packet)
            if (particleType != null) {

                val location = packet.toLorenzVec().toBlockPos().down().toLorenzVec()
                if (location in recentlyDugParticleBurrows) return
                val burrow = burrows.getOrPut(location) { Burrow(location) }

                when (particleType) {
                    ParticleType.FOOTSTEP -> burrow.hasFootstep = true
                    ParticleType.ENCHANT -> burrow.hasEnchant = true
                    ParticleType.EMPTY -> burrow.type = 0
                    ParticleType.MOB -> burrow.type = 1
                    ParticleType.TREASURE -> burrow.type = 2
                }

                if (burrow.hasEnchant && burrow.hasFootstep && burrow.type != -1) {
                    if (!burrow.found) {
                        BurrowDetectEvent(burrow.location, burrow.getType()).post()
                        burrow.found = true
                    }
                }
            }
        }
    }

    private enum class ParticleType(val check: S2APacketParticles.() -> Boolean) {
        EMPTY({
            particleType == net.minecraft.util.EnumParticleTypes.CRIT_MAGIC && particleCount == 4 && particleSpeed == 0.01f && xOffset == 0.5f && yOffset == 0.1f && zOffset == 0.5f
        }),
        MOB({
            particleType == net.minecraft.util.EnumParticleTypes.CRIT && particleCount == 3 && particleSpeed == 0.01f && xOffset == 0.5f && yOffset == 0.1f && zOffset == 0.5f

        }),
        TREASURE({
            particleType == net.minecraft.util.EnumParticleTypes.DRIP_LAVA && particleCount == 2 && particleSpeed == 0.01f && xOffset == 0.35f && yOffset == 0.1f && zOffset == 0.35f
        }),
        FOOTSTEP({
            particleType == net.minecraft.util.EnumParticleTypes.FOOTSTEP && particleCount == 1 && particleSpeed == 0.0f && xOffset == 0.05f && yOffset == 0.0f && zOffset == 0.05f
        }),
        ENCHANT({
            particleType == net.minecraft.util.EnumParticleTypes.ENCHANTMENT_TABLE && particleCount == 5 && particleSpeed == 0.05f && xOffset == 0.5f && yOffset == 0.4f && zOffset == 0.5f
        });

        companion object {

            fun getParticleType(packet: S2APacketParticles): ParticleType? {
                if (!packet.isLongDistance) return null
                for (type in entries) {
                    if (type.check(packet)) {
                        return type
                    }
                }
                return null
            }
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        reset()
    }

    fun reset() {
        burrows.clear()
        recentlyDugParticleBurrows.clear()
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!isEnabled()) return
        if (!config.burrowsSoopyGuess) return
        val message = event.message
        if (message.startsWith("§eYou dug out a Griffin Burrow!") ||
            message == "§eYou finished the Griffin burrow chain! §r§7(4/4)"
        ) {
            BurrowAPI.lastBurrowRelatedChatMessage = SimpleTimeMark.now()
            val burrow = lastDugParticleBurrow
            if (burrow != null) {
                if (!tryDig(burrow)) {
                    fakeBurrow = burrow
                }
            }
        }
        if (message == "§cDefeat all the burrow defenders in order to dig it!") {
            BurrowAPI.lastBurrowRelatedChatMessage = SimpleTimeMark.now()
        }
    }

    private fun tryDig(location: LorenzVec, ignoreFound: Boolean = false): Boolean {
        val burrow = burrows[location] ?: return false
        if (!burrow.found && !ignoreFound) return false
        burrows.remove(location)
        recentlyDugParticleBurrows.add(location)
        lastDugParticleBurrow = null

        BurrowDugEvent(burrow.location).post()
        return true
    }

    @HandleEvent
    fun onBlockClick(event: BlockClickEvent) {
        if (!isEnabled()) return
        if (!config.burrowsSoopyGuess) return

        val location = event.position
        if (event.itemInHand?.isDianaSpade != true || location.getBlockAt() !== Blocks.grass) return

        if (location == fakeBurrow) {
            fakeBurrow = null
            // This exists to detect the unlucky timing when the user opens a burrow before it gets fully detected
            tryDig(location, ignoreFound = true)
            return
        }

        if (burrows.containsKey(location)) {
            lastDugParticleBurrow = location

            DelayedRun.runDelayed(1.seconds) {
                if (BurrowAPI.lastBurrowRelatedChatMessage.passedSince() > 2.seconds) {
                    burrows.remove(location)
                }
            }
        }
    }

    class Burrow(
        var location: LorenzVec,
        var hasFootstep: Boolean = false,
        var hasEnchant: Boolean = false,
        var type: Int = -1,
        var found: Boolean = false,
    ) {

        fun getType(): BurrowType {
            return when (this.type) {
                0 -> BurrowType.START
                1 -> BurrowType.MOB
                2 -> BurrowType.TREASURE
                else -> BurrowType.UNKNOWN
            }
        }
    }

    private fun isEnabled() = DianaAPI.isDoingDiana()
}
