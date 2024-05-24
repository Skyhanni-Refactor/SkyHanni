package at.hannibal2.skyhanni.features.nether.ashfang

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.entity.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.minecraft.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.features.combat.damageindicator.BossType
import at.hannibal2.skyhanni.features.combat.damageindicator.DamageIndicatorManager
import at.hannibal2.skyhanni.utils.ItemUtils.name
import net.minecraft.entity.item.EntityArmorStand

object AshfangHideParticles {

    private var nearAshfang = false

    @HandleEvent(onlyOnSkyblock = true)
    fun onSecondPassed(event: SecondPassedEvent) {
        if (event.repeatSeconds(3)) {
            nearAshfang = DamageIndicatorManager.getDistanceTo(BossType.NETHER_ASHFANG) < 40
        }
    }

    @HandleEvent
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (isEnabled()) {
            event.cancel()
        }
    }

    @HandleEvent(priority = HandleEvent.HIGH)
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (!isEnabled()) return

        val entity = event.entity
        if (entity is EntityArmorStand) {
            for (stack in entity.inventory) {
                if (stack == null) continue
                val name = stack.name
                if (name == "§aFairy Souls") continue
                if (name == "Glowstone") {
                    event.cancel()
                }
            }
        }
    }

    private fun isEnabled() =
        SkyBlockAPI.isConnected && SkyHanniMod.feature.crimsonIsle.ashfang.hide.particles && nearAshfang
}
