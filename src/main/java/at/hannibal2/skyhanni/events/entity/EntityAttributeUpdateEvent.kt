package at.hannibal2.skyhanni.events.entity

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import net.minecraft.entity.Entity
import net.minecraft.entity.ai.attributes.IAttribute
import net.minecraft.network.play.server.S20PacketEntityProperties

data class EntityAttributeUpdateEvent(
    val entity: Entity,
    val attributes: Map<String, Double>,
) : SkyHanniEvent() {

    internal constructor(entity: Entity, snapshots: List<S20PacketEntityProperties.Snapshot>) : this(
        entity,
        // Attribute to base
        snapshots.associate { it.func_151409_a() to it.func_151410_b() },
    )

    fun getAttribute(attribute: IAttribute): Double? {
        return attributes[attribute.attributeUnlocalizedName]
    }
}
