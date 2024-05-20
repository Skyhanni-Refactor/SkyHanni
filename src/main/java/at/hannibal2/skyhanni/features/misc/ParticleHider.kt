package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.ReceiveParticleEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.mc.McWorld
import net.minecraft.entity.projectile.EntitySmallFireball
import net.minecraft.util.EnumParticleTypes

object ParticleHider {

    private fun inM7Boss() = DungeonAPI.inDungeon() && DungeonAPI.dungeonFloor == "M7" && DungeonAPI.inBossRoom

    @HandleEvent
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        val distanceToPlayer = event.distanceToPlayer
        if (SkyHanniMod.feature.misc.particleHiders.hideFarParticles && distanceToPlayer > 40 && !inM7Boss()) {
            event.cancel()
            return
        }

        val type = event.type
        if (SkyHanniMod.feature.misc.particleHiders.hideCloseRedstoneParticles && type == EnumParticleTypes.REDSTONE && distanceToPlayer < 2) {
            event.cancel()
            return
        }

        if (SkyHanniMod.feature.misc.particleHiders.hideFireballParticles && (type == EnumParticleTypes.SMOKE_NORMAL || type == EnumParticleTypes.SMOKE_LARGE)) {
            for (entity in McWorld.getEntitiesOf<EntitySmallFireball>()) {
                val distance = entity.getLorenzVec().distance(event.location)
                if (distance < 5) {
                    event.cancel()
                    return
                }
            }
        }
    }
}
