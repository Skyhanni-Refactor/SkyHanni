package at.hannibal2.skyhanni.utils.mc

import at.hannibal2.skyhanni.mixins.transformers.AccessorGuiContainer
import at.hannibal2.skyhanni.mixins.transformers.AccessorGuiEditSign
import at.hannibal2.skyhanni.utils.StringUtils.capAtMinecraftLength
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.inventory.ContainerChest
import net.minecraft.util.ChatComponentText

object McScreen {

    var screen: GuiScreen?
        get() = McClient.minecraft.currentScreen
        set(value) = McClient.minecraft.displayGuiScreen(value)

    val isOpen get() = screen != null

    val isSignOpen get() = screen is GuiEditSign

    val isChestOpen get() = screen is GuiChest

    val asChest get() = screen as? GuiChest

    val GuiChest.name get() = (inventorySlots as ContainerChest).lowerChestInventory.displayName.unformattedText.trim()

    val GuiContainer.left get() = (this as AccessorGuiContainer).guiLeft
    val GuiContainer.top get() = (this as AccessorGuiContainer).guiTop

    val GuiEditSign.text get() = (this as? AccessorGuiEditSign)?.tileSign?.signText?.map { it.unformattedText }

    fun GuiScreen.setTextIntoSign(text: String, line: Int = 0) {
        if (this !is AccessorGuiEditSign) return
        this.tileSign.signText[line] = ChatComponentText(text)
    }

    fun GuiScreen.addTextIntoSign(addedText: String) {
        if (this !is AccessorGuiEditSign) return
        val lines = this.tileSign.signText
        val index = this.editLine
        val text = lines[index].unformattedText + addedText
        lines[index] = ChatComponentText(text.capAtMinecraftLength(91))
    }
}
