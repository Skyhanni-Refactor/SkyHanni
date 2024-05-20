package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.inventory.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.inventory.InventoryUpdatedEvent
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object HoppityNpc {

    private val config get() = HoppityEggsManager.config

    private var slotsToHighlight = mutableSetOf<Int>()
    private var inShop = false

    @HandleEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return
        if (event.inventoryName != "Hoppity") return
        inShop = true
    }

    private fun clear() {
        inShop = false
        slotsToHighlight.clear()
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        clear()
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        clear()
    }

    @HandleEvent
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        if (!inShop) return
        slotsToHighlight.clear()
        for ((slot, item) in event.inventoryItems) {
            if (item.getLore().contains("§eClick to trade!")) {
                slotsToHighlight.add(slot)
            }
        }
    }

    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!inShop) return
        for (slot in InventoryUtils.getItemsInOpenChest()) {
            if (slot.slotIndex in slotsToHighlight) {
                slot highlight LorenzColor.GREEN.addOpacity(200)
            }
        }
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.highlightHoppityShop
}
