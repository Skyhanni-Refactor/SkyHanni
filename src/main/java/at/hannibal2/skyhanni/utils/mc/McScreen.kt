package at.hannibal2.skyhanni.utils.mc

import at.hannibal2.skyhanni.mixins.transformers.gui.AccessorGuiContainer
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.ContainerChest

object McScreen {

    var screen: GuiScreen?
        get() = McClient.minecraft.currentScreen
        set(value) = McClient.minecraft.displayGuiScreen(value)

    val isOpen get() = screen != null

    val isSignOpen get() = screen is net.minecraft.client.gui.inventory.GuiEditSign

    val isChestOpen get() = screen is GuiChest

    val asChest get() = screen as? GuiChest

    val GuiChest.name get() = (inventorySlots as ContainerChest).lowerChestInventory.displayName.unformattedText.trim()

    val GuiContainer.left get() = (this as AccessorGuiContainer).guiLeft
    val GuiContainer.top get() = (this as AccessorGuiContainer).guiTop
}
