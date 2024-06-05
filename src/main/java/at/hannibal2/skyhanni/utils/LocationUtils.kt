package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.mc.McPlayer
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity

object LocationUtils {

    fun canSee(a: LorenzVec, b: LorenzVec) =
        Minecraft.getMinecraft().theWorld.rayTraceBlocks(a.toVec3(), b.toVec3(), false, true, false) == null

    fun playerLocation() = McPlayer.pos

    fun LorenzVec.distanceToPlayer() = distance(playerLocation())

    fun LorenzVec.distanceToPlayerIgnoreY() = distanceIgnoreY(playerLocation())

    fun LorenzVec.distanceSqToPlayer() = distanceSq(playerLocation())

    fun LorenzVec.distanceToPlayerSqIgnoreY() = distanceSqIgnoreY(playerLocation())

    fun Entity.distanceToPlayer() = getLorenzVec().distanceToPlayer()

    fun Entity.distanceTo(location: LorenzVec) = getLorenzVec().distance(location)
    fun Entity.distanceTo(other: Entity) = getLorenzVec().distance(other.getLorenzVec())

    fun playerEyeLocation(): LorenzVec {
        val player = Minecraft.getMinecraft().thePlayer
        val vec = player.getLorenzVec()
        return vec.add(y = player.getEyeHeight().toDouble())
    }

    fun LorenzVec.canBeSeen(radius: Double = 150.0): Boolean {
        val a = playerEyeLocation()
        val b = this
        val noBlocks = canSee(a, b)
        val notTooFar = a.distance(b) < radius
        val inFov = true // TODO add Frustum "Frustum().isBoundingBoxInFrustum(entity.entityBoundingBox)"
        return noBlocks && notTooFar && inFov
    }

    fun LorenzVec.canBeSeen(yOffsetRange: IntRange, radius: Double = 150.0): Boolean =
        yOffsetRange.any { offset ->
            this.add(y = offset).canBeSeen(radius)
        }
}
