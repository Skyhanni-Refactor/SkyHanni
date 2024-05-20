package at.hannibal2.skyhanni.events.entity

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import net.minecraft.entity.EntityLivingBase

class EntityMaxHealthUpdateEvent(
    val entity: EntityLivingBase,
    val normalizedMaxHealth: Int,
    val actualMaxHealth: Int,
) : SkyHanniEvent()
