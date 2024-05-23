package at.hannibal2.skyhanni.features.slayer.blaze

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.utils.ColourUtils.withAlpha
import net.minecraft.entity.EntityLiving

object HellionShieldHelper {
    val hellionShieldMobs = mutableMapOf<EntityLiving, HellionShield>()

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        hellionShieldMobs.clear()
    }

    fun EntityLiving.setHellionShield(shield: HellionShield?) {
        if (shield != null) {
            hellionShieldMobs[this] = shield
            RenderLivingEntityHelper.setEntityColorWithNoHurtTime(
                this,
                shield.color.toColor().withAlpha(80)
            ) { SkyBlockAPI.isConnected && SkyHanniMod.feature.slayer.blazes.hellion.coloredMobs }
        } else {
            hellionShieldMobs.remove(this)
            RenderLivingEntityHelper.removeCustomRender(this)
        }
    }
}
