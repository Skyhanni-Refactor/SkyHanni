package at.hannibal2.skyhanni.features.anvil

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.render.gui.BackgroundDrawnEvent
import at.hannibal2.skyhanni.utils.InventoryUtils.getInventoryName
import at.hannibal2.skyhanni.utils.InventoryUtils.getLowerItems
import at.hannibal2.skyhanni.utils.InventoryUtils.getUpperItems
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest

object AnvilCombineHelper {

    @HandleEvent
    fun onBackgroundDrawn(event: BackgroundDrawnEvent) {
        if (!SkyBlockAPI.isConnected) return
        if (!SkyHanniMod.feature.inventory.anvilCombineHelper) return

        if (event.gui !is GuiChest) return
        val chest = event.gui.inventorySlots as ContainerChest
        val chestName = chest.getInventoryName()

        if (chestName != "Anvil") return

        val matchLore = mutableListOf<String>()

        for ((slot, stack) in chest.getUpperItems()) {
            if (slot.slotNumber == 29) {
                val lore = stack.getLore()
                matchLore.addAll(lore)
                break
            }
        }

        if (matchLore.isEmpty()) return

        for ((slot, stack) in chest.getLowerItems()) {
            if (matchLore == stack.getLore()) {
                slot highlight LorenzColor.GREEN
            }
        }
    }
}
