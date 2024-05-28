package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.render.gui.SlotClickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import net.minecraft.client.gui.inventory.GuiChest

@SkyHanniModule
object ShiftClickBrewing {

    @HandleEvent(onlyOnSkyblock = true)
    fun onSlotClick(event: SlotClickEvent) {
        if (!SkyHanniMod.feature.inventory.shiftClickBrewing) return

        if (event.gui !is GuiChest) return

        if (event.slot == null) return

        val chestName = InventoryUtils.openInventoryName()
        if (!chestName.startsWith("Brewing Stand")) return

        event.makeShiftClick()
    }
}
