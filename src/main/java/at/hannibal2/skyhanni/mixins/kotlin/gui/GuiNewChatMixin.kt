package at.hannibal2.skyhanni.mixins.kotlin.gui

import at.hannibal2.skyhanni.features.chat.ChatPeek.peek
import at.hannibal2.skyhanni.kmixin.annotations.InjectionKind
import at.hannibal2.skyhanni.kmixin.annotations.KInject
import at.hannibal2.skyhanni.kmixin.annotations.KInjectAt
import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import at.hannibal2.skyhanni.kmixin.annotations.TargetShift
import at.hannibal2.skyhanni.mixins.hooks.FontRendererHook
import net.minecraft.client.gui.GuiNewChat
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@KMixin(GuiNewChat::class)
object GuiNewChatMixin {

    @KInject(method = "getChatOpen", kind = InjectionKind.HEAD, cancellable = true)
    fun onIsOpen(cir: CallbackInfoReturnable<Boolean>) {
        if (peek()) cir.returnValue = true
    }

    @KInjectAt(
        method = "drawChat",
        target = "Lnet/minecraft/client/renderer/GlStateManager;enableBlend()V",
        shift = TargetShift.AFTER
    )
    fun setTextRenderIsFromChat(updateCounter: Int, ci: CallbackInfo) {
        FontRendererHook.cameFromChat = true
    }

    @KInjectAt(
        method = "drawChat",
        target = "Lnet/minecraft/client/renderer/GlStateManager;disableAlpha()V",
        shift = TargetShift.BEFORE
    )
    fun setTextRenderIsNotFromChat(updateCounter: Int, ci: CallbackInfo) {
        FontRendererHook.cameFromChat = false
    }
}
