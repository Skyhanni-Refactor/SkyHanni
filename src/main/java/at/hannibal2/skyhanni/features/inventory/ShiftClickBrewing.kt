package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.render.gui.SlotClickEvent
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import net.minecraft.client.gui.inventory.GuiChest

object ShiftClickBrewing {

    @HandleEvent
    fun onSlotClick(event: SlotClickEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!SkyHanniMod.feature.inventory.shiftClickBrewing) return

        if (event.gui !is GuiChest) return

        if (event.slot == null) return

        val chestName = InventoryUtils.openInventoryName()
        if (!chestName.startsWith("Brewing Stand")) return

        event.makeShiftClick()
    }
}
