package at.hannibal2.skyhanni.mixins.kotlin

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import at.hannibal2.skyhanni.kmixin.annotations.KRedirectCall
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.util.EnumParticleTypes
import net.minecraft.world.World

@KMixin(EntityEnderman::class)
object EntityEndermanMixin {

    @KRedirectCall(method = "onLivingUpdate", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V")
    fun onLivingUpdate(
        world: World,
        particleType: EnumParticleTypes,
        x: Double, y: Double, z: Double,
        xOffset: Double, yOffset: Double, zOffset: Double,
        parameters: IntArray
    ) {
        if (!SkyHanniMod.feature.misc.particleHiders.hideEndermanParticles) {
            world.spawnParticle(particleType, x, y, z, xOffset, yOffset, zOffset, *parameters)
        }
    }
}
