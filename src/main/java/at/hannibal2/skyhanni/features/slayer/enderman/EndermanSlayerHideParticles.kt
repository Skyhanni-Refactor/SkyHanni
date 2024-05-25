package at.hannibal2.skyhanni.features.slayer.enderman

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.minecraft.ReceiveParticleEvent
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.mc.McWorld
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.util.EnumParticleTypes

object EndermanSlayerHideParticles {

    private var endermanLocations = listOf<LorenzVec>()

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!isEnabled()) return

        endermanLocations = McWorld.getEntitiesOf<EntityEnderman>().map { it.getLorenzVec() }.toList()
    }

    @HandleEvent
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (!isEnabled()) return

        when (event.type) {
            EnumParticleTypes.SMOKE_LARGE,
            EnumParticleTypes.FLAME,
            EnumParticleTypes.SPELL_WITCH,
            -> {
            }

            else -> return
        }

        val distance = event.location.distanceToNearestEnderman() ?: return
        if (distance < 9) {
            event.cancel()
        }
    }

    private fun LorenzVec.distanceToNearestEnderman() = endermanLocations.minOfOrNull { it.distanceSq(this) }

    fun isEnabled() = IslandType.THE_END.isInIsland() && SkyHanniMod.feature.slayer.endermen.hideParticles
}
