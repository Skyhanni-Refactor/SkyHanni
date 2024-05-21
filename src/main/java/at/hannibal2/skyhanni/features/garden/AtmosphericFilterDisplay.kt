package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature
import at.hannibal2.skyhanni.events.render.gui.GuiRenderEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.datetime.SkyblockSeason

object AtmosphericFilterDisplay {

    private val config get() = SkyHanniMod.feature.garden.atmosphericFilterDisplay

    private var display = ""

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        if (!GardenAPI.inGarden() && !config.outsideGarden) return
        display = drawDisplay(SkyblockSeason.getCurrentSeason() ?: return)
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (GardenAPI.inGarden()) {
            config.position.renderString(display, posLabel = "Atmospheric Filter Perk Display")
        } else {
            config.positionOutside.renderString(display, posLabel = "Atmospheric Filter Perk Display")
        }
    }

    private fun drawDisplay(season: SkyblockSeason): String = buildString {
        if (!config.onlyBuff) {
            append(season.getSeason(config.abbreviateSeason))
            append("§7: ")
        }
        append(season.getPerk(config.abbreviatePerk))
    }

    private fun isEnabled() = HypixelAPI.onHypixel && config.enabled && (
        (OutsideSbFeature.ATMOSPHERIC_FILTER.isSelected() && !LorenzUtils.inSkyBlock) ||
            (LorenzUtils.inSkyBlock && (GardenAPI.inGarden() || config.outsideGarden))
        )
}
