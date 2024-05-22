package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.inventory.InventoryOpenEvent
import at.hannibal2.skyhanni.events.render.gui.BackgroundDrawnEvent
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.highlight

object HighlightOnHoverSlot {
    val currentSlots = mutableMapOf<Pair<Int, Int>, List<Int>>()

    @HandleEvent
    fun onInventoryOpen(event: InventoryOpenEvent) {
        currentSlots.clear()
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        currentSlots.clear()
    }

    @HandleEvent(priority = HandleEvent.LOW)
    fun onDrawBackground(event: BackgroundDrawnEvent) {
        if (!LorenzUtils.inSkyBlock) return
        val list = currentSlots.flatMapTo(mutableSetOf()) { it.value }
        for (slot in InventoryUtils.getItemsInOpenChest()) {
            if (slot.slotNumber in list) {
                slot highlight LorenzColor.GREEN
            }
        }
    }
}
