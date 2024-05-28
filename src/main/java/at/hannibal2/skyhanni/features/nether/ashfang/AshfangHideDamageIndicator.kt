package at.hannibal2.skyhanni.features.nether.ashfang

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.render.entity.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.features.combat.damageindicator.BossType
import at.hannibal2.skyhanni.features.combat.damageindicator.DamageIndicatorManager
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.minecraft.entity.EntityLivingBase

@SkyHanniModule
object AshfangHideDamageIndicator {

    @HandleEvent(priority = HandleEvent.HIGH, generic = EntityLivingBase::class)
    fun onRenderLiving(event: SkyHanniRenderEntityEvent.Specials.Pre<EntityLivingBase>) {
        if (!isEnabled()) return

        if (DamageIndicatorManager.isDamageSplash(event.entity)) {
            event.cancel()
        }
    }

    private fun isEnabled() =
        SkyBlockAPI.isConnected && SkyHanniMod.feature.crimsonIsle.ashfang.hide.damageSplash &&
            DamageIndicatorManager.isBossSpawned(BossType.NETHER_ASHFANG)
}
