package at.hannibal2.skyhanni.events.render.entity

import at.hannibal2.skyhanni.api.event.GenericSkyHanniEvent
import net.minecraft.entity.Entity

open class EntityRenderLayersEvent<T : Entity> protected constructor(
    val entity: T
) : GenericSkyHanniEvent<T>(entity.javaClass) {

    class Pre<T : Entity>(
        entity: T,
    ) : EntityRenderLayersEvent<T>(entity), Cancellable
}
