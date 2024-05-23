package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.entity.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.entity.EntityEquipmentChangeEvent
import at.hannibal2.skyhanni.events.minecraft.ReceiveParticleEvent
import at.hannibal2.skyhanni.utils.CollectionUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getSkullTexture
import at.hannibal2.skyhanni.utils.LocationUtils.distanceTo
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.EnumParticleTypes

object LesserOrbHider {

    private val config get() = SkyHanniMod.feature.misc
    private val hiddenEntities = CollectionUtils.weakReferenceList<EntityArmorStand>()

    private const val LESSER_ORB_TEXTURE =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjgzMjM2NjM5NjA3MDM2YzFiYTM5MWMyYjQ2YTljN2IwZWZkNzYwYzhiZmEyOTk2YTYwNTU1ODJiNGRhNSJ9fX0="

    @HandleEvent
    fun onArmorChange(event: EntityEquipmentChangeEvent) {
        val entity = event.entity
        val itemStack = event.newItemStack ?: return

        if (entity is EntityArmorStand && event.isHand && itemStack.getSkullTexture() == LESSER_ORB_TEXTURE) {
            hiddenEntities.add(entity)
        }
    }

    @HandleEvent
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (!isEnabled()) return

        if (event.entity in hiddenEntities) {
            event.cancel()
        }
    }

    @HandleEvent
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (!isEnabled()) return
        if (event.type != EnumParticleTypes.REDSTONE) return

        for (armorStand in hiddenEntities) {
            val distance = armorStand.distanceTo(event.location)
            if (distance < 4) {
                event.cancel()
            }
        }
    }

    fun isEnabled() = SkyBlockAPI.isConnected && config.lesserOrbHider
}
