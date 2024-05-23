package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.inventory.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.render.gui.SlotClickEvent
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

object ShiftClickNPCSell {

    private val config get() = SkyHanniMod.feature.inventory.shiftClickNPCSell

    private val sellSlot = -4
    private val lastLoreLineOfSellPattern by RepoPattern.pattern(
        "inventory.npc.sell.lore",
        "§7them to this Shop!|§eClick to buyback!"
    )

    var inInventory = false
        private set

    fun isEnabled() = SkyBlockAPI.isConnected && config

    @HandleEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!SkyBlockAPI.isConnected) return
        if (event.inventoryItems.isEmpty()) return
        val item = event.inventoryItems[event.inventoryItems.keys.last() + sellSlot] ?: return

        inInventory = lastLoreLineOfSellPattern.matches(item.getLore().lastOrNull())
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
    }

    @HandleEvent
    fun onSlotClick(event: SlotClickEvent) {
        if (!isEnabled()) return
        if (!inInventory) return

        val slot = event.slot ?: return

        if (slot.slotNumber == slot.slotIndex) return

        event.makeShiftClick()
    }
}
