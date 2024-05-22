package at.hannibal2.skyhanni.mixins.hooks

import at.hannibal2.skyhanni.data.GuiData
import at.hannibal2.skyhanni.data.OtherInventoryData
import at.hannibal2.skyhanni.events.render.gui.BackgroundDrawnEvent
import at.hannibal2.skyhanni.events.render.gui.BeforeDrawEvent
import at.hannibal2.skyhanni.events.render.gui.DrawScreenAfterEvent
import at.hannibal2.skyhanni.events.render.gui.ForegroundDrawnEvent
import at.hannibal2.skyhanni.events.render.gui.SlotClickEvent
import at.hannibal2.skyhanni.test.SkyHanniDebugsAndTests
import io.github.moulberry.notenoughupdates.NEUApi
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Slot
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

class GuiContainerHook(guiAny: Any) {

    val gui: GuiContainer = guiAny as GuiContainer

    fun closeWindowPressed(ci: CallbackInfo) {
        OtherInventoryData.close()
    }

    fun backgroundDrawn(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!SkyHanniDebugsAndTests.globalRender) return
        BackgroundDrawnEvent(gui, gui.inventorySlots, mouseX, mouseY, partialTicks).post()
    }

    fun preDraw(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
        ci: CallbackInfo,
    ) {
        if (!SkyHanniDebugsAndTests.globalRender) return
        if (BeforeDrawEvent(gui, gui.inventorySlots, mouseX, mouseY, partialTicks).post()) {
            NEUApi.setInventoryButtonsToDisabled()
            GuiData.preDrawEventCanceled = true
            ci.cancel()
        } else {
            GuiData.preDrawEventCanceled = false
        }
    }

    fun foregroundDrawn(mouseX: Int, mouseY: Int, partialTicks: Float) {
        ForegroundDrawnEvent(gui, gui.inventorySlots, mouseX, mouseY, partialTicks).post()
    }

    fun onMouseClick(slot: Slot?, slotId: Int, clickedButton: Int, clickType: Int, ci: CallbackInfo) {
        val item = gui.inventorySlots?.inventory?.takeIf { it.size > slotId && slotId >= 0 }?.get(slotId)
        if (SlotClickEvent(gui, gui.inventorySlots, item, slot, slotId, clickedButton, clickType).post()) ci.cancel()
    }

    fun onDrawScreenAfter(
        mouseX: Int,
        mouseY: Int,
        ci: CallbackInfo,
    ) {
        if (DrawScreenAfterEvent(mouseX, mouseY).post()) ci.cancel()
    }

}
