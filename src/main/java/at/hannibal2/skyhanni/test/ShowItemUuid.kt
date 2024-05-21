package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.item.SkyHanniToolTipEvent
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getItemUuid

object ShowItemUuid {

    @HandleEvent
    fun onTooltip(event: SkyHanniToolTipEvent) {
        if (!SkyHanniMod.feature.dev.debug.showItemUuid) return
        event.itemStack.getItemUuid()?.let {
            event.toolTip.add("§7Item UUID: '$it'")
        }
    }
}
