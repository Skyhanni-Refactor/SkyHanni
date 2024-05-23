package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.jsonobjects.repo.LocationFixJson
import at.hannibal2.skyhanni.events.utils.RepositoryReloadEvent
import at.hannibal2.skyhanni.utils.math.BoundingBox
import at.hannibal2.skyhanni.utils.mc.McPlayer

object LocationFixData {

    private var locationFixes = mutableListOf<LocationFix>()

    class LocationFix(val island: IslandType, val area: BoundingBox, val realLocation: String)

    // priority set to low (1) so that IslandType can load their island names from repo earlier
    @HandleEvent(priority = HandleEvent.LOW)
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
