package at.hannibal2.skyhanni.features.combat.mobs

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.render.entity.SkyHanniRenderEntityEvent
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand

object AshfangMinisNametagHider {

    private val config get() = SkyHanniMod.feature.combat.mobs

    @HandleEvent(priority = HandleEvent.HIGH, generic = EntityLivingBase::class)
    fun onRenderLiving(event: SkyHanniRenderEntityEvent.Specials.Pre<EntityLivingBase>) {
        if (!SkyBlockAPI.isConnected) return
        if (!config.hideNameTagArachneMinis) return

        val entity = event.entity
        if (entity !is EntityArmorStand) return
        if (!entity.hasCustomName()) return

        val name = entity.name
        if (name.contains("§cArachne's Brood§r")) {
            event.cancel()
        }
    }
}
