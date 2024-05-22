package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.inventory.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.item.SkyHanniToolTipEvent
import at.hannibal2.skyhanni.events.render.gui.BackgroundDrawnEvent
import at.hannibal2.skyhanni.events.render.gui.SlotClickEvent
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarApi
import at.hannibal2.skyhanni.utils.CollectionUtils.nextAfter
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getPrice
import at.hannibal2.skyhanni.utils.NumberUtil
import at.hannibal2.skyhanni.utils.RenderUtils.highlight

object PowerStoneGuideFeatures {

    private var missing = mutableMapOf<Int, NEUInternalName>()
    private var inInventory = false

    @HandleEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return
        if (event.inventoryName != "Power Stones Guide") return

        inInventory = true

        for ((slot, item) in event.inventoryItems) {
            val lore = item.getLore()
            if (lore.contains("§7Learned: §cNot Yet ✖")) {
                val rawName = lore.nextAfter("§7Power stone:") ?: continue
                val name = NEUInternalName.fromItemName(rawName)
                missing[slot] = name
            }
        }
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
    }

    @HandleEvent
    fun onBackgroundDrawn(event: BackgroundDrawnEvent) {
        if (!isEnabled()) return
        if (!inInventory) return

        event.gui.inventorySlots.inventorySlots
            .filter { missing.containsKey(it.slotNumber) }
            .forEach { it highlight LorenzColor.RED }
    }

    @HandleEvent
    fun onSlotClick(event: SlotClickEvent) {
        if (!isEnabled()) return
        if (!inInventory) return
        val internalName = missing[event.slotId] ?: return

        BazaarApi.searchForBazaarItem(internalName.itemName, 9)
    }

    @HandleEvent
    fun onTooltip(event: SkyHanniToolTipEvent) {
        if (!isEnabled()) return
        if (!inInventory) return

        val internalName = missing[event.slot.slotNumber] ?: return
        val totalPrice = internalName.getPrice() * 9
        event.toolTip.add(5, "9x from Bazaar: §6${NumberUtil.format(totalPrice)}")
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && SkyHanniMod.feature.inventory.powerStoneGuide
}
