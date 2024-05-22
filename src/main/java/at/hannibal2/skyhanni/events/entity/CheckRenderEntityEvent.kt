package at.hannibal2.skyhanni.events.entity

import at.hannibal2.skyhanni.api.event.CancellableSkyHanniEvent
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.entity.Entity

data class CheckRenderEntityEvent<T : Entity>(
    val entity: T,
    val camera: ICamera,
    val camX: Double,
    val camY: Double,
    val camZ: Double,
) : CancellableSkyHanniEvent()
