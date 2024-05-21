package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.render.gui.RenderInventoryItemTipEvent
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld

object TestShowSlotNumber {

    @HandleEvent
    fun onRenderItemTip(event: RenderInventoryItemTipEvent) {
        if (SkyHanniMod.feature.dev.showSlotNumberKey.isKeyHeld()) {
            val slotIndex = event.slot.slotIndex
            event.stackTip = "$slotIndex"
        }
    }
}
