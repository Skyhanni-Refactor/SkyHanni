package at.hannibal2.skyhanni.utils.mc

typealias Screen = net.minecraft.client.gui.GuiScreen

object McScreen {

    var screen: Screen?
        get() = McClient.minecraft.currentScreen
        set(value) = McClient.minecraft.displayGuiScreen(value)

    val isOpen get() = screen != null

    val isSignOpen get() = screen is net.minecraft.client.gui.inventory.GuiEditSign

    val isChestOpen get() = screen is net.minecraft.client.gui.inventory.GuiChest
}
