package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.render.gui.SlotClickEvent
import at.hannibal2.skyhanni.utils.InventoryUtils
import net.minecraft.client.gui.inventory.GuiChest

object ShiftClickEquipment {

    @HandleEvent(onlyOnSkyblock = true)
    fun onSlotClick(event: SlotClickEvent) {
        if (!SkyHanniMod.feature.inventory.shiftClickForEquipment) return

        if (event.gui !is GuiChest) return

        val slot = event.slot ?: return

        if (slot.slotNumber == slot.slotIndex) return

        if (slot.stack == null) return

        val chestName = InventoryUtils.openInventoryName()
        if (!chestName.startsWith("Your Equipment")) return

        event.makeShiftClick()
    }
}
