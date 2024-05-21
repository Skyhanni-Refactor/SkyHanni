package at.hannibal2.skyhanni.features.mining.crystalhollows

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.math.BoundingBox
import net.minecraft.client.Minecraft
import java.awt.Color

object CrystalHollowsWalls {

    private val config get() = SkyHanniMod.feature.mining.crystalHollowsAreaWalls

    fun isEnabled() = config.enabled && IslandType.CRYSTAL_HOLLOWS.isInIsland()

    private enum class Areas(val color: Color) {
        MITHRIL(LorenzColor.GREEN.addOpacity(60)),
        PRECURSOR(LorenzColor.BLUE.addOpacity(60)),
        JUNGLE(LorenzColor.LIGHT_PURPLE.addOpacity(60)),
        GOBLIN(LorenzColor.GOLD.addOpacity(60)),
        HEAT(LorenzColor.RED.addOpacity(60)),
        NUCLEUS(LorenzColor.WHITE.addOpacity(60))
        ;
    }

    private const val EXPAND_TIMES = 20

    private const val HEAT_HEIGHT = 64.0
    private const val MAX_HEIGHT = 190.0

    private const val MIN_X = 0.0
    private const val MIDDLE_X = 513.0
    private const val MAX_X = 1024.0

    private const val MIN_Z = 0.0
    private const val MIDDLE_Z = 513.0
    private const val MAX_Z = 1024.0

    private val yViewOffset get() = -Minecraft.getMinecraft().thePlayer.getEyeHeight().toDouble()

    // Yes Hypixel has misaligned the nucleus
    private val nucleusBB = BoundingBox(
        463.0, HEAT_HEIGHT, 460.0,
        560.0, MAX_HEIGHT, 563.0
    )

    private val nucleusBBInflate = nucleusBB.expand(LorenzVec.expandVector * -EXPAND_TIMES)
    private val nucleusBBExpand = nucleusBB.expand(LorenzVec.expandVector * EXPAND_TIMES)

    private val nucleusBBOffsetY get() = nucleusBB.move(0.0, yViewOffset, 0.0)

    private fun Double.shiftPX() = this + LorenzVec.expandVector.x * EXPAND_TIMES
    private fun Double.shiftNX() = this - LorenzVec.expandVector.x * EXPAND_TIMES

    private fun Double.shiftPY() = this + LorenzVec.expandVector.y * EXPAND_TIMES
    private fun Double.shiftNY() = this - LorenzVec.expandVector.y * EXPAND_TIMES

    private fun Double.shiftPZ() = this + LorenzVec.expandVector.z * EXPAND_TIMES
    private fun Double.shiftNZ() = this - LorenzVec.expandVector.z * EXPAND_TIMES

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return
        val position = RenderUtils.getViewerPos(event.partialTicks)
        if (position.y < HEAT_HEIGHT + yViewOffset) {
            drawHeat(event)
        } else if (nucleusBBOffsetY.contains(position)) {
            if (!config.nucleus) return
            drawNucleus(event)
        } else if (position.x > MIDDLE_X) {
            if (position.z > MIDDLE_Z) {
                drawPrecursor(event)
            } else {
                drawMithril((event))
            }
        } else {
            if (position.z > MIDDLE_Z) {
                drawGoblin(event)
            } else {
                drawJungle(event)
            }
        }
    }

    private fun drawGoblin(event: SkyHanniRenderWorldEvent) = RenderUtils.QuadDrawer.draw3D(event.partialTicks) {
        drawArea(true, false, Areas.JUNGLE.color, Areas.PRECURSOR.color)
    }

    private fun drawJungle(event: SkyHanniRenderWorldEvent) = RenderUtils.QuadDrawer.draw3D(event.partialTicks) {
        drawArea(true, true, Areas.GOBLIN.color, Areas.MITHRIL.color)
    }

    private fun drawPrecursor(event: SkyHanniRenderWorldEvent) = RenderUtils.QuadDrawer.draw3D(event.partialTicks) {
        drawArea(false, false, Areas.MITHRIL.color, Areas.GOBLIN.color)
    }

    private fun drawMithril(event: SkyHanniRenderWorldEvent) = RenderUtils.QuadDrawer.draw3D(event.partialTicks) {
        drawArea(false, true, Areas.PRECURSOR.color, Areas.JUNGLE.color)
    }

    private fun drawHeat(event: SkyHanniRenderWorldEvent) = RenderUtils.QuadDrawer.draw3D(event.partialTicks) {
        val heatHeight = HEAT_HEIGHT.shiftNY()
        draw(
            LorenzVec(nucleusBB.minX, heatHeight, nucleusBB.minZ),
            LorenzVec(nucleusBB.maxX, heatHeight, nucleusBB.minZ),
            LorenzVec(nucleusBB.minX, heatHeight, nucleusBB.maxZ),
            Areas.NUCLEUS.color
        )

        drawHeatAreaForHeat(false, false, Areas.PRECURSOR.color, heatHeight)
        drawHeatAreaForHeat(false, true, Areas.MITHRIL.color, heatHeight)
        drawHeatAreaForHeat(true, false, Areas.GOBLIN.color, heatHeight)
        drawHeatAreaForHeat(true, true, Areas.JUNGLE.color, heatHeight)
    }

    private fun drawNucleus(event: SkyHanniRenderWorldEvent) {
        val (southEastB, southWestB, northEastB, northWestB) = nucleusBBInflate.getBottomCorners()
        val (southWestT, southEastT, northEastT, northWestT) = nucleusBBInflate.getTopCorners()

        RenderUtils.QuadDrawer.draw3D(event.partialTicks) {
            draw(
                southEastB,
                southWestB,
                northEastB,
                Areas.HEAT.color
            )
            draw(
                southEastB,
                southEastT,
                LorenzVec(nucleusBBInflate.minX, nucleusBBInflate.minY, MIDDLE_Z),
                Areas.JUNGLE.color
            )
            draw(
                southEastB,
                southEastT,
                LorenzVec(MIDDLE_X, nucleusBBInflate.minY, nucleusBBInflate.minZ),
                Areas.JUNGLE.color
            )
            draw(
                northWestB,
                northWestT,
                LorenzVec(nucleusBBInflate.maxX, nucleusBBInflate.minY, MIDDLE_Z),
                Areas.PRECURSOR.color
            )
            draw(
                northWestB,
                northWestT,
                LorenzVec(MIDDLE_X, nucleusBBInflate.minY, nucleusBBInflate.maxZ),
                Areas.PRECURSOR.color
            )
            draw(
                southWestB,
                southWestT,
                LorenzVec(nucleusBBInflate.minX, nucleusBBInflate.minY, MIDDLE_Z),
                Areas.GOBLIN.color,
            )
            draw(
                southWestB,
                southWestT,
                LorenzVec(MIDDLE_X, nucleusBBInflate.minY, nucleusBBInflate.maxZ),
                Areas.GOBLIN.color
            )
            draw(
                northEastB,
                northEastT,
                LorenzVec(nucleusBBInflate.maxX, nucleusBBInflate.minY, MIDDLE_Z),
                Areas.MITHRIL.color
            )
            draw(
                northEastB,
                northEastT,
                LorenzVec(MIDDLE_X, nucleusBBInflate.minY, nucleusBBInflate.minZ),
                Areas.MITHRIL.color
            )
        }
    }

    private fun RenderUtils.QuadDrawer.drawArea(
        isMinXEsleMaxX: Boolean,
        isMinZElseMaxZ: Boolean,
        color1: Color,
        color2: Color,
    ) {
        val nucleusX = if (isMinXEsleMaxX) nucleusBBExpand.minX else nucleusBBExpand.maxX
        val middleX = if (isMinXEsleMaxX) MIDDLE_X.shiftNX() else MIDDLE_X.shiftPX()
        val x = if (isMinXEsleMaxX) MIN_X else MAX_X

        val nucleusZ = if (isMinZElseMaxZ) nucleusBBExpand.minZ else nucleusBBExpand.maxZ
        val middleZ = if (isMinZElseMaxZ) MIDDLE_Z.shiftNZ() else MIDDLE_Z.shiftPZ()
        val z = if (isMinZElseMaxZ) MIN_Z else MAX_Z

        val heatHeight = HEAT_HEIGHT.shiftPY()

        val nucleusBase = LorenzVec(nucleusX, heatHeight, nucleusZ)

        val nucleusZSideBase = LorenzVec(middleX, heatHeight, nucleusZ)
        val nucleusXSideBase = LorenzVec(nucleusX, heatHeight, middleZ)

        drawHeatArea(
            Areas.HEAT.color,
            heatHeight,
            nucleusX,
            middleX,
            x,
            nucleusZ,
            middleZ,
            z
        )
        draw(
            nucleusXSideBase,
            LorenzVec(nucleusX, MAX_HEIGHT, middleZ),
            LorenzVec(x, heatHeight, middleZ),
            color1,
        )
        draw(
            nucleusZSideBase,
            LorenzVec(middleX, MAX_HEIGHT, nucleusZ),
            LorenzVec(middleX, heatHeight, z),
            color2,
        )
        draw(
            nucleusXSideBase,
            nucleusBase,
            LorenzVec(nucleusX, MAX_HEIGHT, middleZ),
            Areas.NUCLEUS.color,
        )
        draw(
            nucleusZSideBase,
            nucleusBase,
            LorenzVec(middleX, MAX_HEIGHT, nucleusZ),
            Areas.NUCLEUS.color,
        )
    }

    private fun RenderUtils.QuadDrawer.drawHeatAreaForHeat(
        isMinXEsleMaxX: Boolean,
        isMinZElseMaxZ: Boolean,
        color: Color,
        heatHeight: Double,
    ) = this.drawHeatArea(
        color,
        heatHeight,
        nucleusX = if (isMinXEsleMaxX) nucleusBB.minX else nucleusBB.maxX,
        middleX = if (isMinXEsleMaxX) MIDDLE_X else MIDDLE_X,
        x = if (isMinXEsleMaxX) MIN_X else MAX_X,
        nucleusZ = if (isMinZElseMaxZ) nucleusBB.minZ else nucleusBB.maxZ,
        middleZ = if (isMinZElseMaxZ) MIDDLE_X else MIDDLE_X,
        z = if (isMinZElseMaxZ) MIN_Z else MAX_Z,
    )

    private fun RenderUtils.QuadDrawer.drawHeatArea(
        color: Color,
        heatHeight: Double,
        nucleusX: Double,
        middleX: Double,
        x: Double,
        nucleusZ: Double,
        middleZ: Double,
        z: Double,
    ) {
        val nucleusBase = LorenzVec(nucleusX, heatHeight, nucleusZ)

        draw(
            nucleusBase,
            LorenzVec(nucleusX, heatHeight, z),
            LorenzVec(middleX, heatHeight, nucleusZ),
            color,
        )
        draw(
            nucleusBase,
            LorenzVec(x, heatHeight, nucleusZ),
            LorenzVec(nucleusX, heatHeight, middleZ),
            color,
        )
        draw(
            nucleusBase,
            LorenzVec(x, heatHeight, nucleusZ),
            LorenzVec(nucleusX, heatHeight, z),
            color,
        )
    }

}
