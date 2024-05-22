package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.render.gui.GuiRenderEvent
import at.hannibal2.skyhanni.events.utils.ConfigLoadEvent
import at.hannibal2.skyhanni.utils.ConditionalUtils

object TrackerManager {

    private var hasChanged = false
    var dirty = false

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        val config = SkyHanniMod.feature.misc.tracker.hideCheapItems
        ConditionalUtils.onToggle(config.alwaysShowBest, config.minPrice, config.enabled) {
            hasChanged = true
        }
    }

    @HandleEvent(HandleEvent.HIGHEST)
    fun onRenderOverlayFirst(event: GuiRenderEvent) {
        if (hasChanged) {
            dirty = true
        }
    }

    @HandleEvent(HandleEvent.LOWEST)
    fun onRenderOverlayLast(event: GuiRenderEvent) {
        if (hasChanged) {
            dirty = false
            hasChanged = false
        }
    }
}
