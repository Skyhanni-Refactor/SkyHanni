package at.hannibal2.skyhanni.mixins.kotlin.renderer

import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.features.chroma.ChromaManager
import at.hannibal2.skyhanni.kmixin.annotations.InjectionKind
import at.hannibal2.skyhanni.kmixin.annotations.KInject
import at.hannibal2.skyhanni.kmixin.annotations.KPseudoMixin
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@KPseudoMixin("club.sk1er.patcher.hooks.FontRendererHook")
object PatcherFontRendererHookMixin {

    @KInject(method = "renderStringAtPos(Ljava/lang/String;Z)Z", kind = InjectionKind.HEAD, cancellable = true)
    fun overridePatcherFontRenderer(string: String, shadow: Boolean, cir: CallbackInfoReturnable<Boolean>) {
        if (!HypixelAPI.onHypixel) return

        if (ChromaManager.config.enabled.get()) {
            cir.cancel()
            cir.returnValue = false
        }
    }
}
