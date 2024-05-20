package at.hannibal2.skyhanni.events.entity

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.utils.LorenzVec
import net.minecraft.entity.Entity

class EntityMoveEvent(
    val entity: Entity,
    val oldLocation: LorenzVec,
    val newLocation: LorenzVec,
    val distance: Double,
) : SkyHanniEvent()
