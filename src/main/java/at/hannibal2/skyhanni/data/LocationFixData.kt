package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.data.jsonobjects.repo.LocationFixJson
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.utils.math.BoundingBox
import at.hannibal2.skyhanni.utils.mc.McPlayer
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object LocationFixData {

    private var locationFixes = mutableListOf<LocationFix>()

    class LocationFix(val island: IslandType, val area: BoundingBox, val realLocation: String)

    // priority set to low so that IslandType can load their island names from repo earlier
    @SubscribeEvent(priority = EventPriority.LOW)
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<LocationFixJson>("LocationFix")
        locationFixes.clear()

        for (fix in data.locationFixes.values) {
            val island = IslandType.getByName(fix.islandName)
            val area = BoundingBox(fix.a, fix.b)
            val realLocation = fix.realLocation

            locationFixes.add(LocationFix(island, area, realLocation))
        }
    }

    fun fixLocation(skyBlockIsland: IslandType) = locationFixes
        .firstOrNull { skyBlockIsland == it.island && it.area.contains(McPlayer.pos) }
        ?.realLocation
}
