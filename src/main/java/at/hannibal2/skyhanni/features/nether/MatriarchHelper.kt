package at.hannibal2.skyhanni.features.nether

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.entity.MobEvent
import at.hannibal2.skyhanni.test.command.CopyNearbyEntitiesCommand.getMobInfo
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ColourUtils.toChromaColour
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine
import at.hannibal2.skyhanni.utils.RenderUtils.drawFilledBoundingBox_nea
import at.hannibal2.skyhanni.utils.RenderUtils.exactPlayerEyeLocation
import at.hannibal2.skyhanni.utils.getLorenzVec
import java.util.TreeSet

object MatriarchHelper {

    private val config get() = SkyHanniMod.feature.crimsonIsle.matriarchHelper

    private val pearlList = TreeSet<Mob> { first, second ->
        first.baseEntity.getLorenzVec().y.compareTo(second.baseEntity.getLorenzVec().y)
    }

    @HandleEvent
    fun onMobSpawn(event: MobEvent.Spawn.Special) {
        if (!isHeavyPearl(event)) return
        pearlList.add(event.mob)
        if (pearlList.size > 3) {
            ErrorManager.logErrorStateWithData(
                "Something went wrong with the Heavy Pearl detection",
                "More then 3 pearls",
                "pearList" to pearlList.map { getMobInfo(it) }
            )
            pearlList.clear()
        }
    }

    private fun isHeavyPearl(event: MobEvent) = isEnabled() && event.mob.name == "Heavy Pearl"

    @HandleEvent
    fun onMobDespawn(event: MobEvent.DeSpawn.Special) {
        if (!isHeavyPearl(event)) return
        pearlList.remove(event.mob)
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return
        if (config.highlight) {
            val color = config.highlightColor.toChromaColour()
            pearlList.forEach {
                event.drawFilledBoundingBox_nea(it.boundingBox.expandToEdge(), color, 1.0f)
            }
        }
        if (config.line) {
            val color = config.lineColor.toChromaColour()
            var prePoint = event.exactPlayerEyeLocation()
            pearlList.forEach {
                val point = it.baseEntity.getLorenzVec().add(y = 1.2)
                event.draw3DLine(prePoint, point, color, 10, true)
                prePoint = point
            }
        }
    }

    fun isEnabled() = config.enabled && IslandType.CRIMSON_ISLE.isInIsland()
}
