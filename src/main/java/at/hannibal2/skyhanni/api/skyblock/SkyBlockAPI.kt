package at.hannibal2.skyhanni.api.skyblock

import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.HypixelData.skyBlockIsland
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.hypixel.HypixelLocationEvent
import at.hannibal2.skyhanni.events.minecraft.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.utils.ProfileJoinEvent
import at.hannibal2.skyhanni.features.bingo.BingoAPI
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import com.mojang.authlib.GameProfile
import java.util.UUID

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

    private val scoreboardVisitingAmountPattern by patternGroup.pattern(
        "scoreboard.visiting.amount",
        "\\s+§.✌ §.\\(§.(?<currentamount>\\d+)§./(?<maxamount>\\d+)\\)"
    )

    private val profileNamePattern by patternGroup.pattern(
        "profile.name",
        "(?:Your profile was changed to: |You are playing on profile: )(?<name>[\\w ]+)(?: \\(Co-op\\))?"
    )
    private val profileIdPattern by patternGroup.pattern(
        "profile.id",
        "Profile ID: (?<id>\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12})"
    )

    var isGuesting: Boolean = false
        private set

    var island: IslandType = IslandType.UNKNOWN
        private set

    val isConnected: Boolean
        get() = HypixelAPI.onHypixel && HypixelAPI.gametype == "SKYBLOCK"

    var gamemode: Gamemode = Gamemode.UNKNOWN
        private set

    var profileName: String? = null
        private set

    var profileId: UUID? = null
        private set

    val maxPlayers: Int
        get() {
            ScoreboardData.sidebarLinesFormatted.matchFirst(scoreboardVisitingAmountPattern) {
                return group("maxamount").toInt()
            }

            return when (skyBlockIsland) {
                IslandType.MINESHAFT -> 4
                IslandType.CATACOMBS -> 5
                IslandType.CRYSTAL_HOLLOWS -> 24
                IslandType.CRIMSON_ISLE -> 24
                else -> if (HypixelAPI.server?.startsWith("mega") == true) 80 else 26
            }
        }


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

    @HandleEvent
    fun onChatMessage(event: SkyHanniChatEvent) {
        if (!HypixelAPI.onHypixel) return

        val message = event.message.removeColor()

        profileNamePattern.matchMatcher(message) {
            val newName = group("name").lowercase()
            if (profileName != newName) {
                profileName = newName
                ProfileJoinEvent(newName).post()
            }
        }

        profileIdPattern.matchMatcher(message) {
            profileId = runCatching { UUID.fromString(group("id")) }.getOrNull()
        }
    }

}
