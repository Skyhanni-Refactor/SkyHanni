package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.Perk
import at.hannibal2.skyhanni.events.entity.EntityAttributeUpdateEvent
import at.hannibal2.skyhanni.events.entity.EntityMaxHealthUpdateEvent
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.item.EntityArmorStand

object EntityAttributeAPI {

    private val HEALTH_ATTRIBUTE = SharedMonsterAttributes.maxHealth

    @HandleEvent
    fun onEntityMaxHealthUpdate(event: EntityAttributeUpdateEvent) {
        val entity = event.entity
        if (entity !is EntityLivingBase) return
        if (entity is EntityArmorStand) return
        val maxHealth = event.getAttribute(HEALTH_ATTRIBUTE) ?: return
        val oldMaxHealth = entity.attributeMap.getAttributeInstance(HEALTH_ATTRIBUTE)?.baseValue ?: -1.0
        if (oldMaxHealth != maxHealth) {
            EntityMaxHealthUpdateEvent(
                entity,
                normalizeHealth(maxHealth),
                maxHealth.toInt()
            ).post()
        }
    }

    // TODO Check for runic and corrupted mobs
    private fun normalizeHealth(maxHealth: Double): Int {
        return when {
            Perk.DOUBLE_MOBS_HP.isActive -> maxHealth / 2.0
            else -> maxHealth
        }.toInt()
    }
}
