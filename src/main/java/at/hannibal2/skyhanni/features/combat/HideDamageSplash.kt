package at.hannibal2.skyhanni.features.combat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.render.entity.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.features.combat.damageindicator.DamageIndicatorManager
import net.minecraft.entity.EntityLivingBase

object HideDamageSplash {

    @HandleEvent(onlyOnSkyblock = true, priority = HandleEvent.HIGH, generic = EntityLivingBase::class)
    fun onRenderLiving(event: SkyHanniRenderEntityEvent.Specials.Pre<EntityLivingBase>) {
        if (!SkyHanniMod.feature.combat.hideDamageSplash) return

        if (DamageIndicatorManager.isDamageSplash(event.entity)) {
            event.cancel()
        }
    }
}
