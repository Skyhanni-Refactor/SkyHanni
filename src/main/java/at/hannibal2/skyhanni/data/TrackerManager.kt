package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.render.gui.GuiRenderEvent
import at.hannibal2.skyhanni.utils.ConditionalUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TrackerManager {

    private var hasChanged = false
    var dirty = false

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        val config = SkyHanniMod.feature.misc.tracker.hideCheapItems
        ConditionalUtils.onToggle(config.alwaysShowBest, config.minPrice, config.enabled) {
            hasChanged = true
        }
    }

    @HandleEvent(priority = -2)
    fun onRenderOverlayFirst(event: GuiRenderEvent) {
        if (hasChanged) {
            dirty = true
        }
    }

    @HandleEvent(priority = 2)
    fun onRenderOverlayLast(event: GuiRenderEvent) {
        if (hasChanged) {
            dirty = false
            hasChanged = false
        }
    }
}
