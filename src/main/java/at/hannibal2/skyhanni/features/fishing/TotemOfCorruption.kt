package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.fishing.TotemOfCorruptionConfig.OutlineType
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.minecraft.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.render.gui.GuiRenderEvent
import at.hannibal2.skyhanni.events.utils.ConfigLoadEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.utils.ColourUtils.toChromaColour
import at.hannibal2.skyhanni.utils.ConditionalUtils.onToggle
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.drawSphereInWorld
import at.hannibal2.skyhanni.utils.RenderUtils.drawSphereWireframeInWorld
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.datetime.TimeUnit
import at.hannibal2.skyhanni.utils.datetime.TimeUtils.format
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.mc.McSound
import at.hannibal2.skyhanni.utils.mc.McSound.play
import at.hannibal2.skyhanni.utils.mc.McWorld
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.EnumParticleTypes
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object TotemOfCorruption {

    private val config get() = SkyHanniMod.feature.fishing.totemOfCorruption

    private var display = emptyList<String>()
    private var totems: List<Totem> = emptyList()

    private val patternGroup = RepoPattern.group("fishing.totemofcorruption")
    private val totemNamePattern by patternGroup.pattern(
        "totemname",
        "§5§lTotem of Corruption"
    )
    private val timeRemainingPattern by patternGroup.pattern(
        "timeremaining",
        "§7Remaining: §e(?:(?<min>\\d+)m )?(?<sec>\\d+)s"
    )
    private val ownerPattern by patternGroup.pattern(
        "owner",
        "§7Owner: §e(?<owner>.+)"
    )

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isOverlayEnabled() || display.isEmpty()) return
        config.position.renderStrings(display, posLabel = "Totem of Corruption")
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!event.repeatSeconds(2)) return
        if (!isOverlayEnabled()) return

        totems = getTotems()
        display = createDisplay()
    }

    @HandleEvent
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (!isHideParticlesEnabled()) return

        for (totem in totems) {
            if (event.type == EnumParticleTypes.SPELL_WITCH && event.speed == 0.0f) {
                if (totem.location.distance(event.location) < 4.0) {
                    event.cancel()
                }
            }
        }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEffectiveAreaEnabled()) return
        if (totems.isEmpty()) return

        val color = config.color.toChromaColour()
        for (totem in totems) {
            // The center of the totem is the upper part of the armor stand
            when (config.outlineType) {
                OutlineType.FILLED -> {
                    event.drawSphereInWorld(color, totem.location.add(y = 1), 16f)
                }

                OutlineType.WIREFRAME -> {
                    event.drawSphereWireframeInWorld(color, totem.location.add(y = 1), 16f)
                }

                else -> return
            }
        }
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        config.showOverlay.onToggle {
            display = emptyList()
            totems = emptyList()
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        display = emptyList()
        totems = emptyList()
    }

    private fun getTimeRemaining(totem: EntityArmorStand): Duration? =
        McWorld.getEntitiesNear<EntityArmorStand>(totem, 2.0)
            .firstNotNullOfOrNull { entity ->
                timeRemainingPattern.matchMatcher(entity.name) {
                    val minutes = group("min")?.toIntOrNull() ?: 0
                    val seconds = group("sec")?.toInt() ?: 0
                    (minutes * 60 + seconds).seconds
                }
            }

    private fun getOwner(totem: EntityArmorStand): String? =
        McWorld.getEntitiesNear<EntityArmorStand>(totem, 2.0)
            .firstNotNullOfOrNull { entity ->
                ownerPattern.matchMatcher(entity.name) {
                    group("owner")
                }
            }

    private fun createDisplay() = buildList {
        val totem = getTotemToShow() ?: return@buildList
        add("§5§lTotem of Corruption")
        add("§7Remaining: §e${totem.timeRemaining.format(TimeUnit.MINUTE)}")
        add("§7Owner: §e${totem.ownerName}")
    }

    private fun getTotemToShow(): Totem? = totems
        .filter { it.distance < config.distanceThreshold }
        .maxByOrNull { it.timeRemaining }

    private fun getTotems(): List<Totem> = McWorld.getEntitiesNearPlayer<EntityArmorStand>(100.0)
        .filter { totemNamePattern.matches(it.name) }
        .toList()
        .mapNotNull { totem ->
            val timeRemaining = getTimeRemaining(totem) ?: return@mapNotNull null
            val owner = getOwner(totem) ?: return@mapNotNull null

            val timeToWarn = config.warnWhenAboutToExpire.seconds
            if (timeToWarn > 0.seconds && timeRemaining == timeToWarn) {
                McSound.PLING.play()
                TitleManager.sendTitle("§c§lTotem of Corruption §eabout to expire!", 5.seconds)
            }
            Totem(totem.getLorenzVec(), timeRemaining, owner)
        }

    private fun isOverlayEnabled() = SkyBlockAPI.isConnected && config.showOverlay.get()
    private fun isHideParticlesEnabled() = SkyBlockAPI.isConnected && config.hideParticles
    private fun isEffectiveAreaEnabled() = SkyBlockAPI.isConnected && config.outlineType != OutlineType.NONE
}

private class Totem(
    val location: LorenzVec,
    val timeRemaining: Duration,
    val ownerName: String,
    val distance: Double = location.distanceToPlayer()
)
