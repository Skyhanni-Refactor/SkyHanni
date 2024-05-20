package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.events.DataWatcherUpdatedEvent
import at.hannibal2.skyhanni.events.entity.EntityCustomNameUpdateEvent
import at.hannibal2.skyhanni.events.entity.EntityHealthUpdateEvent
import at.hannibal2.skyhanni.utils.LorenzUtils.derpy
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.DataWatcher
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.entity.item.EntityXPOrb
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object DataWatcherAPI {

    private const val DATA_VALUE_CUSTOM_NAME = 2
    private const val DATA_VALUE_HEALTH = 6

    @SubscribeEvent
    fun onDataWatcherUpdate(event: DataWatcherUpdatedEvent) {
        for (updatedEntry in event.updatedEntries) {
            when (updatedEntry.dataValueId) {
                DATA_VALUE_CUSTOM_NAME -> onCustomNameUpdate(event.entity)
                DATA_VALUE_HEALTH -> onHealthUpdate(event.entity, updatedEntry)
            }
        }
    }

    private fun onCustomNameUpdate(entity: Entity) {
        EntityCustomNameUpdateEvent(entity.customNameTag, entity).post()
    }

    private fun onHealthUpdate(entity: Entity, entry: DataWatcher.WatchableObject) {
        if (entity !is EntityLivingBase) return
        if (entity is EntityArmorStand) return
        if (entity is EntityXPOrb) return
        if (entity is EntityItem) return
        if (entity is EntityItemFrame) return
        if (entity is EntityOtherPlayerMP) return
        if (entity is EntityPlayerSP) return

        val health = (entry.`object` as Float).toInt()

        if (entity is EntityWither && health == 300 && entity.entityId < 0) return

        EntityHealthUpdateEvent(entity, health.derpy()).post()
    }
}
