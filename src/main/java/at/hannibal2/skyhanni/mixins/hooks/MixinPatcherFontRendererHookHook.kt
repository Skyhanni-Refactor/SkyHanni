package at.hannibal2.skyhanni.mixins.hooks

import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.features.chroma.ChromaManager
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

class MixinPatcherFontRendererHookHook {
    companion object {

        @JvmStatic
        fun overridePatcherFontRenderer(string: String, shadow: Boolean, cir: CallbackInfoReturnable<Boolean>) {
            if (!HypixelAPI.onHypixel) return

            if (ChromaManager.config.enabled.get()) {
                cir.cancel()
                cir.returnValue = false
            }
        }
    }
}
