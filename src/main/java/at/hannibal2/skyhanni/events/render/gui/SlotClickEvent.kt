package at.hannibal2.skyhanni.events.render.gui

import at.hannibal2.skyhanni.api.event.CancellableSkyHanniEvent
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

data class SlotClickEvent(
    val gui: GuiContainer,
    val container: Container,
    val item: ItemStack?,
    val slot: Slot?,
    val slotId: Int,
    val clickedButton: Int,
    @Deprecated("old", ReplaceWith("clickTypeEnum"))
    val clickType: Int,
    val clickTypeEnum: ClickType? = ClickType.getTypeById(clickType),
) : CancellableSkyHanniEvent() {

    fun makePickblock() {
        if (this.clickedButton == 2 && this.clickTypeEnum == ClickType.MIDDLE) return
        slot?.slotNumber?.let { slotNumber ->
            Minecraft.getMinecraft().playerController.windowClick(
                container.windowId, slotNumber, 2, 3, Minecraft.getMinecraft().thePlayer
            )
            cancel()
        }
    }

    fun makeShiftClick() {
        if (this.clickedButton == 1 && slot?.stack?.getItemCategoryOrNull() == ItemCategory.SACK) return
        slot?.slotNumber?.let { slotNumber ->
            Minecraft.getMinecraft().playerController.windowClick(
                container.windowId, slotNumber, 0, 1, Minecraft.getMinecraft().thePlayer
            )
            cancel()
        }
    }

    enum class ClickType(val id: Int) {
        NORMAL(0),
        SHIFT(1),
        HOTBAR(2),
        MIDDLE(3),
        DROP(4),
        ;

        companion object {
            fun getTypeById(id: Int) = entries.firstOrNull { it.id == id }
        }
    }
}


