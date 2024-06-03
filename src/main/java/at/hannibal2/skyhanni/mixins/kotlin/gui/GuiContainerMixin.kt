package at.hannibal2.skyhanni.mixins.kotlin.gui

import at.hannibal2.skyhanni.data.GuiData
import at.hannibal2.skyhanni.data.OtherInventoryData
import at.hannibal2.skyhanni.data.ToolTipData
import at.hannibal2.skyhanni.events.render.gui.BackgroundDrawnEvent
import at.hannibal2.skyhanni.events.render.gui.BeforeDrawEvent
import at.hannibal2.skyhanni.events.render.gui.DrawScreenAfterEvent
import at.hannibal2.skyhanni.events.render.gui.ForegroundDrawnEvent
import at.hannibal2.skyhanni.events.render.gui.SlotClickEvent
import at.hannibal2.skyhanni.kmixin.annotations.InjectionKind
import at.hannibal2.skyhanni.kmixin.annotations.KInject
import at.hannibal2.skyhanni.kmixin.annotations.KInjectAt
import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import at.hannibal2.skyhanni.kmixin.annotations.KSelf
import at.hannibal2.skyhanni.kmixin.annotations.KShadow
import at.hannibal2.skyhanni.kmixin.annotations.TargetShift
import at.hannibal2.skyhanni.test.SkyHanniDebugsAndTests
import io.github.moulberry.notenoughupdates.NEUApi
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Slot
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@KMixin(GuiContainer::class)
object GuiContainerMixin {

    @KInjectAt(
        method = "keyTyped",
        target = "Lnet/minecraft/client/entity/EntityPlayerSP;closeScreen()V",
        shift = TargetShift.BEFORE,
        cancellable = true
    )
    fun closeWindowPressed(ci: CallbackInfo) {
        OtherInventoryData.close()
    }

    @KInjectAt(method = "drawScreen", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", ordinal = 1)
    fun backgroundDrawn(mouseX: Int, mouseY: Int, partialTicks: Float, ci: CallbackInfo, @KSelf gui: GuiContainer) {
        if (!SkyHanniDebugsAndTests.globalRender) return
        BackgroundDrawnEvent(gui, gui.inventorySlots, mouseX, mouseY, partialTicks).post()
    }

    @KInject(method = "drawScreen", kind = InjectionKind.HEAD, cancellable = true)
    fun preDraw(mouseX: Int, mouseY: Int, partialTicks: Float, ci: CallbackInfo, @KSelf gui: GuiContainer) {
        if (!SkyHanniDebugsAndTests.globalRender) return
        if (BeforeDrawEvent(gui, gui.inventorySlots, mouseX, mouseY, partialTicks).post()) {
            NEUApi.setInventoryButtonsToDisabled()
            GuiData.preDrawEventCanceled = true
            ci.cancel()
        } else {
            GuiData.preDrawEventCanceled = false
        }
    }

    @KInjectAt(
        method = "drawScreen",
        target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGuiContainerForegroundLayer(II)V",
        shift = TargetShift.AFTER
    )
    fun onForegroundDraw(mouseX: Int, mouseY: Int, partialTicks: Float, ci: CallbackInfo, @KSelf gui: GuiContainer) {
        ForegroundDrawnEvent(gui, gui.inventorySlots, mouseX, mouseY, partialTicks).post()
    }

    @KInjectAt(
        method = "handleMouseClick",
        target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;windowClick(IIIILnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;",
        cancellable = true
    )
    fun onMouseClick(slot: Slot?, slotId: Int, clickedButton: Int, clickType: Int, ci: CallbackInfo, @KSelf gui: GuiContainer) {
        val item = gui.inventorySlots?.inventory?.takeIf { it.size > slotId && slotId >= 0 }?.get(slotId)
        if (SlotClickEvent(gui, gui.inventorySlots, item, slot, slotId, clickedButton, clickType).post()) ci.cancel()
    }

    @KInjectAt(
        method = "drawScreen",
        target = "Lnet/minecraft/entity/player/InventoryPlayer;getItemStack()Lnet/minecraft/item/ItemStack;",
        shift = TargetShift.BEFORE,
        ordinal = 1
    )
    fun drawScreenAfter(mouseX: Int, mouseY: Int, partialTicks: Float, ci: CallbackInfo, @KShadow theSlot: Slot?) {
        if (DrawScreenAfterEvent(mouseX, mouseY).post()) ci.cancel()
        ToolTipData.lastSlot = theSlot
    }

}
