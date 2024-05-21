package at.hannibal2.skyhanni.api.skyblock

import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.events.hypixel.HypixelLocationEvent
import at.hannibal2.skyhanni.events.minecraft.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.features.bingo.BingoAPI
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

object SkyBlockAPI {

    private val patternGroup = RepoPattern.group("skyblockapi")

    private val ironmanPattern by patternGroup.pattern(
        "ironman",
        " §7♲ §7Ironman"
    )

    private val strandedPattern by patternGroup.pattern(
        "stranded",
        " §a☀ §aStranded"
    )

    var isGuesting: Boolean = false
        private set

    var island: IslandType = IslandType.UNKNOWN
        private set

    val isConnected: Boolean
        get() = HypixelAPI.onHypixel && HypixelAPI.gametype == "SKYBLOCK"

    var gamemode: Gamemode = Gamemode.UNKNOWN
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
        if (HypixelData.guestPattern.matches(ScoreboardData.objectiveTitle.removeColor()) != isGuesting) {
            isGuesting = !isGuesting
            island = when (island) {
                IslandType.PRIVATE_ISLAND -> IslandType.PRIVATE_ISLAND_GUEST
                IslandType.PRIVATE_ISLAND_GUEST -> IslandType.PRIVATE_ISLAND
                IslandType.GARDEN -> IslandType.GARDEN_GUEST
                IslandType.GARDEN_GUEST -> IslandType.GARDEN
                else -> island
            }
        }

        gamemode = Gamemode.CLASSIC
        event.scoreboard.forEach {
            when {
                ironmanPattern.matches(it) -> Gamemode.IRONMAN
                strandedPattern.matches(it) -> Gamemode.STRANDED
                BingoAPI.getRankFromScoreboard(it) != null -> Gamemode.BINGO
                else -> null
            }?.let { mode ->
                gamemode = mode
                return@forEach
            }
        }
    }

}
