package at.hannibal2.skyhanni.mixins.kotlin

import at.hannibal2.skyhanni.events.utils.neu.NeuRenderEvent
import at.hannibal2.skyhanni.kmixin.annotations.InjectionKind
import at.hannibal2.skyhanni.kmixin.annotations.KInject
import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import io.github.moulberry.notenoughupdates.NEUOverlay
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@KMixin(NEUOverlay::class)
object NEUOverlayMixin {

    @KInject(method = "render", kind = InjectionKind.HEAD, cancellable = true, remap = false)
    fun render(hoverInv: Boolean, ci: CallbackInfo) {
        if (NeuRenderEvent().post()) {
            ci.cancel()
        }
    }
}
