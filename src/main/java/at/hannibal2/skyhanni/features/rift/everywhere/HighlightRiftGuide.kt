package at.hannibal2.skyhanni.features.rift.everywhere

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.inventory.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.render.gui.BackgroundDrawnEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

object HighlightRiftGuide {

    private var inInventory = false
    private var highlightedItems = emptyList<Int>()

    private val riftGuideInventoryPattern by RepoPattern.pattern(
        "rift.guide.inventory",
        "Rift Guide ➜.*"
    )

    @HandleEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        inInventory = false
        if (!isEnabled()) return
        if (!riftGuideInventoryPattern.matches(event.inventoryName)) return
        inInventory = true

        val highlightedItems = mutableListOf<Int>()
        for ((slot, stack) in event.inventoryItems) {
            val lore = stack.getLore()
            if (lore.isNotEmpty() && lore.last() == "§8✖ Not completed yet!") {
                highlightedItems.add(slot)
            }
        }
        this.highlightedItems = highlightedItems
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
    }

    @HandleEvent(priority = HandleEvent.LOW)
    fun onBackgroundDrawn(event: BackgroundDrawnEvent) {
        if (!isEnabled()) return
        if (!inInventory) return

        for (slot in InventoryUtils.getItemsInOpenChest()) {
            if (slot.slotIndex in highlightedItems) {
                slot.highlight(LorenzColor.YELLOW)
            }
        }
    }

    fun isEnabled() = RiftAPI.inRift() && RiftAPI.config.highlightGuide
}
