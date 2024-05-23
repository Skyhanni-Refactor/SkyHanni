package at.hannibal2.skyhanni.features.stranded

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.Gamemode
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.inventory.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.render.gui.BackgroundDrawnEvent
import at.hannibal2.skyhanni.events.utils.ConfigFixEvent
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

object HighlightPlaceableNpcs {

    private val config get() = SkyHanniMod.feature.misc.stranded

    private val locationPattern by RepoPattern.pattern(
        "stranded.highlightplacement.location",
        "§7Location: §f\\[§e\\d+§f, §e\\d+§f, §e\\d+§f]"
    )

    private var inInventory = false
    private var highlightedItems = emptyList<Int>()

    @HandleEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        inInventory = false
        if (!isEnabled()) return

        if (event.inventoryName != "Island NPCs") return

        val highlightedItems = mutableListOf<Int>()
        for ((slot, stack) in event.inventoryItems) {
            if (isPlaceableNpc(stack.getLore())) {
                highlightedItems.add(slot)
            }
        }
        inInventory = true
        this.highlightedItems = highlightedItems
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
        highlightedItems = emptyList()
    }

    @HandleEvent(priority = HandleEvent.LOW)
    fun onBackgroundDrawn(event: BackgroundDrawnEvent) {
        if (!isEnabled() || !inInventory) return
        for (slot in InventoryUtils.getItemsInOpenChest()) {
            if (slot.slotIndex in highlightedItems) {
                slot highlight LorenzColor.GREEN
            }
        }
    }

    private fun isPlaceableNpc(lore: List<String>): Boolean {
        // Checking if NPC & placeable
        if (lore.isEmpty() || !(lore.last() == "§ethis NPC!" || lore.last() == "§eyour location!")) {
            return false
        }

        // Checking if is already placed
        return lore.none { locationPattern.matches(it) }
    }

    private fun isEnabled() = SkyBlockAPI.isConnected && SkyBlockAPI.gamemode == Gamemode.STRANDED && config.highlightPlaceableNpcs

    @HandleEvent
    fun onConfigFix(event: ConfigFixEvent) {
        event.move(31, "stranded", "misc.stranded")
    }
}
