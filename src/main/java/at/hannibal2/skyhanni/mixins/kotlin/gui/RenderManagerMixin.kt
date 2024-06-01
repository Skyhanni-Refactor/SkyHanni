package at.hannibal2.skyhanni.mixins.kotlin.gui

import at.hannibal2.skyhanni.events.entity.CheckRenderEntityEvent
import at.hannibal2.skyhanni.kmixin.annotations.InjectionKind
import at.hannibal2.skyhanni.kmixin.annotations.KInject
import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.entity.Entity
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@KMixin(RenderManager::class)
object RenderManagerMixin {

    @KInject(method = "shouldRender", kind = InjectionKind.HEAD, cancellable = true)
    fun shouldRender(
        entity: Entity,
        camera: ICamera,
        camX: Double,
        camY: Double,
        camZ: Double,
        cir: CallbackInfoReturnable<Boolean?>
    ) {
        if (CheckRenderEntityEvent(entity, camera, camX, camY, camZ).post()) {
            cir.returnValue = false
        }
    }
}
