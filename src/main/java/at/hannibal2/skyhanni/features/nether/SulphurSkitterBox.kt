package at.hannibal2.skyhanni.features.nether

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.config.features.crimsonisle.SulphurSkitterBoxConfig
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.utils.ConfigFixEvent
import at.hannibal2.skyhanni.features.fishing.FishingAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColourUtils.toChromaColourInt
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.math.BoundingBox
import at.hannibal2.skyhanni.utils.mc.McWorld.getBlockAt
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import java.awt.Color

@SkyHanniModule
object SulphurSkitterBox {

    private val config get() = SkyHanniMod.feature.fishing.trophyFishing.sulphurSkitterBox
    private var spongeBlocks = listOf<BlockPos>()
    private var closestBlock: BlockPos? = null
    private const val RADIUS = 8

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!isEnabled()) return
        if (event.isMod(5)) {
            closestBlock = getClosestBlockToPlayer()
        }
        if (event.repeatSeconds(1)) {
            val location = LocationUtils.playerLocation()
            val from = location.add(-20, -20, -20).toBlockPos()
            val to = location.add(20, 20, 20).toBlockPos()

            spongeBlocks = BlockPos.getAllInBox(from, to).filter {
                val loc = it.toLorenzVec()
                loc.getBlockAt() == Blocks.sponge && loc.distanceToPlayer() <= 15
            }.filter {
                val pos1 = it.add(-RADIUS, -RADIUS, -RADIUS)
                val pos2 = it.add(RADIUS, RADIUS, RADIUS)
                BlockPos.getAllInBox(pos1, pos2).any { pos ->
                    pos.toLorenzVec().getBlockAt() in FishingAPI.lavaBlocks
                }
            }
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        spongeBlocks = emptyList()
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return
        closestBlock?.let {
            val vec = it.toLorenzVec()
            if (vec.distanceToPlayer() >= 50) return
            val pos1 = vec.add(-RADIUS, -RADIUS, -RADIUS)
            val pos2 = vec.add(RADIUS, RADIUS, RADIUS)
            val axis = BoundingBox(pos1, pos2).expandToEdge()

            drawBox(axis, event.partialTicks)
        }
    }

    private fun getClosestBlockToPlayer(): BlockPos? {
        return spongeBlocks.minByOrNull { it.toLorenzVec().distanceToPlayer() }
    }

    private fun drawBox(axis: BoundingBox, partialTicks: Float) {
        val color = Color(config.boxColor.toChromaColourInt(), true)
        when (config.boxType) {
            SulphurSkitterBoxConfig.BoxType.FULL -> {
                RenderUtils.drawFilledBoundingBox(
                    axis,
                    color,
                    partialTicks = partialTicks,
                    renderRelativeToCamera = false
                )
            }

            SulphurSkitterBoxConfig.BoxType.WIREFRAME -> {
                RenderUtils.drawWireframeBoundingBox(axis, color, partialTicks)
            }

            else -> {
                RenderUtils.drawWireframeBoundingBox(axis, color, partialTicks)
            }
        }
    }

    fun isEnabled() =
        IslandType.CRIMSON_ISLE.isInIsland() && config.enabled && (!config.onlyWithRods || FishingAPI.holdingLavaRod)

    @HandleEvent
    fun onConfigFix(event: ConfigFixEvent) {
        event.move(24, "crimsonIsle.sulphurSkitterBoxConfig", "fishing.trophyFishing.sulphurSkitterBox")
    }
}
