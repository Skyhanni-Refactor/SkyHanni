package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.minecraft.ClientDisconnectEvent
import at.hannibal2.skyhanni.events.minecraft.ScreenChangeEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.render.gui.SlotClickEvent
import at.hannibal2.skyhanni.events.utils.neu.NeuRenderEvent
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import io.github.moulberry.notenoughupdates.NEUApi
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

object GuiData {

    var preDrawEventCanceled = false

    @HandleEvent(HandleEvent.HIGH)
    fun onNeuRenderEvent(event: NeuRenderEvent) {
        if (preDrawEventCanceled) event.cancel()
    }

    @HandleEvent(priority = HandleEvent.HIGH)
    fun onClick(event: SlotClickEvent) {
        if (preDrawEventCanceled) event.cancel()
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onGuiClick(event: GuiScreenEvent.MouseInputEvent.Pre) {
        if (preDrawEventCanceled) event.isCanceled = true
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onGuiKeyPress(event: GuiScreenEvent.KeyboardInputEvent.Pre) {
        val (escKey, invKey) = Minecraft.getMinecraft().gameSettings.let {
            Keyboard.KEY_ESCAPE to it.keyBindInventory.keyCode
        }
        if (escKey.isKeyHeld() || invKey.isKeyHeld()) return
        if (preDrawEventCanceled) event.isCanceled = true
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        DelayedRun.runNextTick {
            if (Minecraft.getMinecraft().currentScreen !is GuiChest) {
                preDrawEventCanceled = false
            }
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        preDrawEventCanceled = false
    }

    @HandleEvent
    fun onDisconnect(event: ClientDisconnectEvent) {
        preDrawEventCanceled = false
    }

    @HandleEvent(priority = HandleEvent.LOW)
    fun onScreenChange(event: ScreenChangeEvent) {
        if (preDrawEventCanceled) {
            NEUApi.setInventoryButtonsToDisabled()
        }
    }
}
