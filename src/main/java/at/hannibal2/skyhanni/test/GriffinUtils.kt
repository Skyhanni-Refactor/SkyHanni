package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.math.BoundingBox
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

object GriffinUtils {

    fun SkyHanniRenderWorldEvent.drawWaypointFilled(
        location: LorenzVec,
        color: Color,
        seeThroughBlocks: Boolean = false,
        beacon: Boolean = false,
        extraSize: Double = 0.0,
        extraSizeTopY: Double = extraSize,
        extraSizeBottomY: Double = extraSize,
    ) {
        val (viewerX, viewerY, viewerZ) = RenderUtils.getViewerPos(partialTicks)
        val x = location.x - viewerX
        val y = location.y - viewerY
        val z = location.z - viewerZ
        val distSq = x * x + y * y + z * z

        if (seeThroughBlocks) {
            GlStateManager.disableDepth()
            GlStateManager.disableCull()
        }
        RenderUtils.drawFilledBoundingBox(
            BoundingBox(
                x - extraSize, y - extraSizeBottomY, z - extraSize,
                x + 1 + extraSize, y + 1 + extraSizeTopY, z + 1 + extraSize
            ).expandToEdge(),
            color,
            (0.1f + 0.005f * distSq.toFloat()).coerceAtLeast(0.2f)
        )
        GlStateManager.disableTexture2D()
        if (distSq > 5 * 5 && beacon) RenderUtils.renderBeaconBeam(x, y + 1, z, color.rgb, 1.0f, partialTicks)
        GlStateManager.disableLighting()
        GlStateManager.enableTexture2D()

        if (seeThroughBlocks) {
            GlStateManager.enableDepth()
            GlStateManager.enableCull()
        }
    }
}
