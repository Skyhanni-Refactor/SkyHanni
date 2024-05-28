package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.render.gui.BackgroundDrawnEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.inventory.Slot

@SkyHanniModule
object HighlightMissingRepoItems {

    @HandleEvent(onlyOnSkyblock = true, priority = HandleEvent.LOWEST)
    fun onBackgroundDrawn(event: BackgroundDrawnEvent) {
        if (!SkyHanniMod.feature.dev.debug.highlightMissingRepo) return

        val gui = event.gui

        if (gui is GuiChest) {
            highlightItems(gui.inventorySlots.inventorySlots)
        } else if (gui is GuiInventory) {
            val player = Minecraft.getMinecraft().thePlayer
            highlightItems(player.inventoryContainer.inventorySlots)
        }
    }

    private fun highlightItems(slots: Iterable<Slot>) {
        if (NEUItems.allInternalNames.isEmpty()) return
        for (slot in slots) {
            val internalName = slot.stack?.getInternalNameOrNull() ?: continue
            if (NEUItems.allInternalNames.contains(internalName)) continue
            if (NEUItems.ignoreItemsFilter.match(internalName.asString())) continue

            slot.highlight(LorenzColor.RED)
        }
    }
}
