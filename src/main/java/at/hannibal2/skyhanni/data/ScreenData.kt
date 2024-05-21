package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import net.minecraft.client.Minecraft

object ScreenData {
    private var wasOpen = false

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        val isOpen = Minecraft.getMinecraft().currentScreen != null
        if (wasOpen == isOpen) return
        wasOpen = isOpen
        if (!wasOpen) {
            InventoryCloseEvent(false).post()
        }
    }
}
