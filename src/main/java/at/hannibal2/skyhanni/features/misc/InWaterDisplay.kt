package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.render.gui.GuiOverlayRenderEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.mc.McPlayer

@SkyHanniModule
object InWaterDisplay {

    private val config get() = SkyHanniMod.feature.misc.stranded

    @HandleEvent(onlyOnSkyblock = true)
    fun onRenderOverlay(event: GuiOverlayRenderEvent) {
        if (!isEnabled()) return

        val text = "§7In Water: " + if (McPlayer.inWater) "§aTrue" else "§cFalse"
        config.inWaterPosition.renderStrings(listOf(text), posLabel = "In Water Display")
    }

    private fun isEnabled() = config.inWaterDisplay
}
