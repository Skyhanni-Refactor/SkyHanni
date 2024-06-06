package at.hannibal2.skyhanni.utils.math

import at.hannibal2.skyhanni.utils.LorenzVec
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import kotlin.math.max
import kotlin.math.min

data class BoundingBox(
    val minX: Double,
    val minY: Double,
    val minZ: Double,
    val maxX: Double,
    val maxY: Double,
    val maxZ: Double,
) {

    constructor(first: LorenzVec, second: LorenzVec) : this(
        minX = first.x.coerceAtMost(second.x),
        minY = first.y.coerceAtMost(second.y),
        minZ = first.z.coerceAtMost(second.z),
        maxX = first.x.coerceAtLeast(second.x),
        maxY = first.y.coerceAtLeast(second.y),
        maxZ = first.z.coerceAtLeast(second.z),
    )

    val min: LorenzVec get() = LorenzVec(minX, minY, minZ)
    val max: LorenzVec get() = LorenzVec(maxX, maxY, maxZ)

    // Expand
    fun expand(x: Double, y: Double, z: Double) =
        BoundingBox(minX - x, minY - y, minZ - z, maxX + x, maxY + y, maxZ + z)

    fun expand(vec: LorenzVec) = expand(vec.x, vec.y, vec.z)
    fun expand(xyz: Double) = expand(xyz, xyz, xyz)
    fun expandToEdge() = expand(LorenzVec.expandVector)

    // Move
    fun move(x: Double, y: Double, z: Double) =
        BoundingBox(minX + x, minY + y, minZ + z, maxX + x, maxY + y, maxZ + z)

    fun move(vec: LorenzVec) = move(vec.x, vec.y, vec.z)

    // Contains
    fun contains(x: Double, y: Double, z: Double) = x in minX..maxX && y in minY..maxY && z in minZ..maxZ
    fun contains(vec: LorenzVec) = contains(vec.x, vec.y, vec.z)
    fun contains(entity: Entity) = contains(entity.posX, entity.posY, entity.posZ)

    fun intersects(origin: LorenzVec, direction: LorenzVec): Boolean {
        // Reference for Algorithm https://tavianator.com/2011/ray_box.html
        val rayDirectionInverse = direction.inverse()
        val t1 = (this.min - origin) * rayDirectionInverse
        val t2 = (this.max - origin) * rayDirectionInverse

        val tmin = max(t1.minOfEachElement(t2).max(), Double.NEGATIVE_INFINITY)
        val tmax = min(t1.maxOfEachElement(t2).min(), Double.POSITIVE_INFINITY)
        return tmax >= tmin && tmax >= 0.0
    }

    // Corners

    fun getTopCorners() = listOf(
        LorenzVec(minX, maxY, minZ),
        LorenzVec(minX, maxY, maxZ),
        LorenzVec(maxX, maxY, maxZ),
        LorenzVec(maxX, maxY, minZ),
    )

    fun getBottomCorners() = listOf(
        LorenzVec(minX, minY, minZ),
        LorenzVec(minX, minY, maxZ),
        LorenzVec(maxX, minY, maxZ),
        LorenzVec(maxX, minY, minZ),
    )

    // Center

    fun getCenter() = LorenzVec(
        (minX + maxX) / 2,
        (minY + maxY) / 2,
        (minZ + maxZ) / 2,
    )

    fun topCenter() = LorenzVec(
        (minX + maxX) / 2,
        maxY,
        (minZ + maxZ) / 2,
    )

    fun bottomCenter() = LorenzVec(
        (minX + maxX) / 2,
        minY,
        (minZ + maxZ) / 2,
    )

    fun union(other: List<BoundingBox>): BoundingBox {
        var minX = this.minX
        var minY = this.minY
        var minZ = this.minZ
        var maxX = this.maxX
        var maxY = this.maxY
        var maxZ = this.maxZ

        other.forEach { box ->
            if (box.minX < minX) minX = box.minX
            if (box.minY < minY) minY = box.minY
            if (box.minZ < minZ) minZ = box.minZ
            if (box.maxX > maxX) maxX = box.maxX
            if (box.maxY > maxY) maxY = box.maxY
            if (box.maxZ > maxZ) maxZ = box.maxZ
        }

        return BoundingBox(minX, minY, minZ, maxX, maxY, maxZ)
    }

    fun toAABB() = AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)
}

fun AxisAlignedBB.toBox() = BoundingBox(minX, minY, minZ, maxX, maxY, maxZ)
