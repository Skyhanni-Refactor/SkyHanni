package at.hannibal2.skyhanni.features.rift.area.dreadfarm

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.minecraft.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawFilledBoundingBox
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.math.BoundingBox
import at.hannibal2.skyhanni.utils.mc.McPlayer
import at.hannibal2.skyhanni.utils.mc.McWorld.getBlockAt
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.util.EnumParticleTypes
import java.awt.Color
import kotlin.time.Duration.Companion.milliseconds

object RiftWiltedBerberisHelper {

    private val config get() = RiftAPI.config.area.dreadfarm.wiltedBerberis
    private var isOnFarmland = false
    private var hasFarmingToolInHand = false
    private var list = listOf<WiltedBerberis>()

    data class WiltedBerberis(var currentParticles: LorenzVec) {

        var previous: LorenzVec? = null
        var moving = true
        var y = 0.0
        var lastTime = SimpleTimeMark.now()
    }

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!isEnabled()) return
        if (!event.isMod(5)) return

        list = list.editCopy { removeIf { it.lastTime.passedSince() > 500.milliseconds } }

        hasFarmingToolInHand = McPlayer.heldItem?.getInternalName() == SkyhanniItems.FARMING_WAND()

        if (Minecraft.getMinecraft().thePlayer.onGround) {
            val block = LocationUtils.playerLocation().add(y = -1).getBlockAt()
            val currentY = LocationUtils.playerLocation().y
            isOnFarmland = block == Blocks.farmland && (currentY % 1 == 0.0)
        }
    }

    private fun nearestBerberis(location: LorenzVec): WiltedBerberis? {
        return list.filter { it.currentParticles.distanceSq(location) < 8 }
            .minByOrNull { it.currentParticles.distanceSq(location) }
    }

    @HandleEvent
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (!isEnabled()) return
        if (!hasFarmingToolInHand) return

        val location = event.location
        val berberis = nearestBerberis(location)

        if (event.type != EnumParticleTypes.FIREWORKS_SPARK) {
            if (config.hideparticles && berberis != null) {
                event.cancel()
            }
            return
        }

        if (config.hideparticles) {
            event.cancel()
        }

        if (berberis == null) {
            list = list.editCopy { add(WiltedBerberis(location)) }
            return
        }

        with(berberis) {
            val isMoving = currentParticles != location
            if (isMoving) {
                if (currentParticles.distance(location) > 3) {
                    previous = null
                    moving = true
                }
                if (!moving) {
                    previous = currentParticles
                }
            }
            if (!isMoving) {
                y = location.y - 1
            }

            moving = isMoving
            currentParticles = location
            lastTime = SimpleTimeMark.now()
        }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return
        if (!hasFarmingToolInHand) return

        if (config.onlyOnFarmland && !isOnFarmland) return

        for (berberis in list) {
            with(berberis) {
                if (currentParticles.distanceToPlayer() > 20) continue
                if (y == 0.0) continue

                val location = currentParticles.fixLocation(berberis)
                if (!moving) {
                    event.drawFilledBoundingBox(axisAlignedBB(location), Color.YELLOW, 0.7f)
                    event.drawDynamicText(location.add(y = 1), "§eWilted Berberis", 1.5, ignoreBlocks = false)
                } else {
                    event.drawFilledBoundingBox(axisAlignedBB(location), Color.WHITE, 0.5f)
                    previous?.fixLocation(berberis)?.let {
                        event.drawFilledBoundingBox(axisAlignedBB(it), Color.LIGHT_GRAY, 0.2f)
                        event.draw3DLine(it.add(0.5, 0.0, 0.5), location.add(0.5, 0.0, 0.5), Color.WHITE, 3, false)
                    }
                }
            }
        }
    }

    private fun axisAlignedBB(loc: LorenzVec): BoundingBox {
        val pos = loc.add(0.1, -0.1, 0.1)
        return BoundingBox(
            pos.x, loc.y, pos.z,
            pos.x + 0.8, loc.y + 1.0, pos.z + 0.8
        ).expandToEdge()
    }

    private fun LorenzVec.fixLocation(wiltedBerberis: WiltedBerberis): LorenzVec {
        val x = x - 0.5
        val y = wiltedBerberis.y
        val z = z - 0.5
        return LorenzVec(x, y, z)
    }

    private fun isEnabled() = RiftAPI.inRift() && RiftAPI.inDreadfarm() && config.enabled
}
