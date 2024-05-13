package at.hannibal2.skyhanni.events

import net.minecraft.entity.EntityLivingBase

class EntityMaxHealthUpdateEvent(
    val entity: EntityLivingBase,
    val normalizedMaxHealth: Int,
    val actualMaxHealth: Int,
) : LorenzEvent()
