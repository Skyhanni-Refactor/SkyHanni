package at.hannibal2.skyhanni.features.slayer.enderman

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.mc.McWorld
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class EndermanSlayerHideParticles {

    private var endermanLocations = listOf<LorenzVec>()

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return

        endermanLocations = McWorld.getEntitiesOf<EntityEnderman>().map { it.getLorenzVec() }.toList()
    }

    @SubscribeEvent
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
            event.isCanceled = true
        }
    }

    private fun LorenzVec.distanceToNearestEnderman() = endermanLocations.minOfOrNull { it.distanceSq(this) }

    fun isEnabled() = IslandType.THE_END.isInIsland() && SkyHanniMod.feature.slayer.endermen.hideParticles
}
