package at.hannibal2.skyhanni.features.fishing.trophy

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.minecraft.ReceiveParticleEvent
import at.hannibal2.skyhanni.features.fishing.FishingAPI
import at.hannibal2.skyhanni.utils.ColourUtils.toChromaColour
import at.hannibal2.skyhanni.utils.LocationUtils.distanceTo
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayerIgnoreY
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.drawFilledBoundingBox_nea
import at.hannibal2.skyhanni.utils.math.BoundingBox
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object GeyserFishing {
    private val config get() = SkyHanniMod.feature.fishing.trophyFishing.geyserOptions

    private val geyserOffset = LorenzVec(0.1f, 0.6f, 0.1f)

    private var geyser: LorenzVec? = null
    private var geyserBox: BoundingBox? = null

    @HandleEvent(priority = 1, receiveCancelled = true)
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (!shouldProcessParticles()) return
        with(event) {
            if (type != EnumParticleTypes.CLOUD || count != 15 || speed != 0.05f || offset != geyserOffset) return
        }
        geyser = event.location
        val potentialGeyser = geyser ?: return

        geyserBox = BoundingBox(
            potentialGeyser.x - 2, 118.0 - 0.1, potentialGeyser.z - 2,
            potentialGeyser.x + 2, 118.0 - 0.09, potentialGeyser.z + 2
        )

        if (config.hideParticles && FishingAPI.bobber != null) {
            hideGeyserParticles(event)
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        geyser = null
        geyserBox = null
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!config.drawBox) return
        val geyserBox = geyserBox ?: return
        val geyser = geyser ?: return
        if (geyser.distanceToPlayerIgnoreY() > 96) return
        if (!IslandType.CRIMSON_ISLE.isInIsland()) return
        if (config.onlyWithRod && !FishingAPI.holdingLavaRod) return

        val color = config.boxColor.toChromaColour()
        event.drawFilledBoundingBox_nea(geyserBox, color)
    }

    private fun hideGeyserParticles(event: ReceiveParticleEvent) {
        val bobber = FishingAPI.bobber ?: return
        val geyser = geyser ?: return

        if (bobber.distanceTo(event.location) < 3 && bobber.distanceTo(geyser) < 3) {
            event.cancel()
        }
    }

    private fun shouldProcessParticles() =
        IslandType.CRIMSON_ISLE.isInIsland() && LorenzUtils.skyBlockArea == "Blazing Volcano" && (config.hideParticles || config.drawBox)
}
