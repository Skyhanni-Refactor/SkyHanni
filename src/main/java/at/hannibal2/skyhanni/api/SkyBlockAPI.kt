package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.events.hypixel.HypixelLocationEvent
import at.hannibal2.skyhanni.events.minecraft.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.removeColor

object SkyBlockAPI {

    var isGuesting: Boolean = false
        private set

    var island: IslandType = IslandType.UNKNOWN
        private set

    private fun reset() {
        isGuesting = false
        island = IslandType.UNKNOWN
    }

    @HandleEvent
    fun onLocationChange(event: HypixelLocationEvent) {
        if (event.type != "SKYBLOCK") return reset()
        island = event.map?.let(IslandType::getByNameOrNull) ?: IslandType.UNKNOWN
    }

    @HandleEvent
    fun onScoreboardUpdate(event: ScoreboardUpdateEvent) {
        val guesting = HypixelData.guestPattern.matches(ScoreboardData.objectiveTitle.removeColor())
        if (guesting != isGuesting) {
            isGuesting = guesting
            island = when (island) {
                IslandType.PRIVATE_ISLAND -> IslandType.PRIVATE_ISLAND_GUEST
                IslandType.PRIVATE_ISLAND_GUEST -> IslandType.PRIVATE_ISLAND
                IslandType.GARDEN -> IslandType.GARDEN_GUEST
                IslandType.GARDEN_GUEST -> IslandType.GARDEN
                else -> island
            }
        }
    }

}
