package at.hannibal2.skyhanni.mixins.kotlin.renderer

import at.hannibal2.skyhanni.data.GuiEditManager
import at.hannibal2.skyhanni.kmixin.annotations.InjectionKind
import at.hannibal2.skyhanni.kmixin.annotations.KInject
import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import net.minecraft.client.renderer.EntityRenderer
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@KMixin(EntityRenderer::class)
object EntityRendererMixin {

    @KInject(method = "updateCameraAndRender", kind = InjectionKind.TAIL)
    fun onRenderWorldLast(partialTicks: Float, nanoTime: Long, ci: CallbackInfo) {
        GuiEditManager.renderLast()
    }
}

