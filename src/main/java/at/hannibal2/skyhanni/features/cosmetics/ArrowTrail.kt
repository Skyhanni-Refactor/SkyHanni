package at.hannibal2.skyhanni.features.cosmetics

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.skyblock.IslandChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColourUtils.toChromaColour
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.getPrevLorenzVec
import at.hannibal2.skyhanni.utils.mc.McPlayer
import at.hannibal2.skyhanni.utils.mc.McWorld
import net.minecraft.entity.projectile.EntityArrow
import java.util.LinkedList
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@SkyHanniModule
object ArrowTrail {

    private val config get() = SkyHanniMod.feature.gui.cosmetic.arrowTrail

    private data class Line(val start: LorenzVec, val end: LorenzVec, val deathTime: SimpleTimeMark)

    private val listAllArrow: MutableList<Line> = LinkedList<Line>()
    private val listYourArrow: MutableList<Line> = LinkedList<Line>()

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!isEnabled()) return
        val secondsAlive = config.secondsAlive.toDouble().toDuration(DurationUnit.SECONDS)
        val time = SimpleTimeMark.now()
        val deathTime = time.plus(secondsAlive)

        listAllArrow.removeIf { it.deathTime.isInPast() }
        listYourArrow.removeIf { it.deathTime.isInPast() }

        McWorld.getEntitiesOf<EntityArrow>().forEach {
            val line = Line(it.getPrevLorenzVec(), it.getLorenzVec(), deathTime)
            if (it.shootingEntity == McPlayer.player) {
                listYourArrow.add(line)
            } else {
                listAllArrow.add(line)
            }
        }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return
        val color = if (config.handlePlayerArrowsDifferently) config.playerArrowColor else config.arrowColor
        val playerArrowColor = color.toChromaColour()
        listYourArrow.forEach {
            event.draw3DLine(it.start, it.end, playerArrowColor, config.lineWidth, true)
        }
        if (!config.hideOtherArrows) {
            val arrowColor = config.arrowColor.toChromaColour()
            listAllArrow.forEach {
                event.draw3DLine(it.start, it.end, arrowColor, config.lineWidth, true)
            }
        }
    }

    private fun isEnabled() = config.enabled && (SkyBlockAPI.isConnected || OutsideSbFeature.ARROW_TRAIL.isSelected())

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        listAllArrow.clear()
        listYourArrow.clear()
    }
}
