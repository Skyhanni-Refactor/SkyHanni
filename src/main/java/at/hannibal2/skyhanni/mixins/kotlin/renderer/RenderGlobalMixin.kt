package at.hannibal2.skyhanni.mixins.kotlin.renderer

import at.hannibal2.skyhanni.kmixin.annotations.InjectionKind
import at.hannibal2.skyhanni.kmixin.annotations.KInject
import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import at.hannibal2.skyhanni.kmixin.annotations.KRedirectCall
import at.hannibal2.skyhanni.kmixin.annotations.KShadow
import at.hannibal2.skyhanni.kmixin.annotations.ShadowKind
import at.hannibal2.skyhanni.utils.EntityOutlineRenderer
import at.hannibal2.skyhanni.utils.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.entity.Entity
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@KMixin(RenderGlobal::class)
object RenderGlobalMixin {

    @KRedirectCall(
        method = "renderEntities",
        target = "Lnet/minecraft/client/renderer/RenderGlobal;isRenderEntityOutlines()Z"
    )
    fun renderEntitiesOutlines(
        self: RenderGlobal?,
        renderViewEntity: Entity?,
        camera: ICamera?,
        partialTicks: Float,
        @KShadow(ShadowKind.METHOD) isRenderEntityOutlines: () -> Boolean
    ): Boolean {
        val vec = RenderUtils.exactLocation(Minecraft.getMinecraft().renderViewEntity, partialTicks)
        return EntityOutlineRenderer.renderEntityOutlines(camera!!, partialTicks, vec) && isRenderEntityOutlines()
    }

    @KInject(method = "isRenderEntityOutlines", kind = InjectionKind.HEAD, cancellable = true)
    fun isRenderEntityOutlinesWrapper(cir: CallbackInfoReturnable<Boolean?>) {
        if (EntityOutlineRenderer.shouldRenderEntityOutlines()) {
            cir.returnValue = true
        }
    }

    @KInject(method = "renderEntityOutlineFramebuffer", kind = InjectionKind.RETURN)
    fun afterFramebufferDraw(callbackInfo: CallbackInfo) {
        GlStateManager.enableDepth()
    }
}
