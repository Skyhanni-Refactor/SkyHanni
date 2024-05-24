package at.hannibal2.skyhanni.api.skyblock

import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.LocationFixData
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.hypixel.HypixelLocationEvent
import at.hannibal2.skyhanni.events.minecraft.ClientDisconnectEvent
import at.hannibal2.skyhanni.events.minecraft.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.skyblock.IslandChangeEvent
import at.hannibal2.skyhanni.events.utils.ProfileJoinEvent
import at.hannibal2.skyhanni.features.bingo.BingoAPI
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TabListData
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
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

    private val playerAmountPattern by patternGroup.pattern(
        "playeramount",
        "^\\s*(?:§.)+Players (?:§.)+\\((?<amount>\\d+)\\)\\s*$"
    )
    private val playerAmountCoopPattern by patternGroup.pattern(
        "playeramount.coop",
        "^\\s*(?:§.)*Coop (?:§.)*\\((?<amount>\\d+)\\)\\s*$"
    )
    private val playerAmountGuestingPattern by patternGroup.pattern(
        "playeramount.guesting",
        "^\\s*(?:§.)*Guests (?:§.)*\\((?<amount>\\d+)\\)\\s*$"
    )
    private val soloProfileAmountPattern by patternGroup.pattern(
        "solo.profile.amount",
        "^\\s*(?:§.)*Island\\s*$"
    )

    private val guestPattern by patternGroup.pattern(
        "guesting.scoreboard",
        "SKYBLOCK GUEST"
    )

    /**
     * REGEX-TEST:  §7⏣ §bVillage
     * REGEX-TEST:  §5ф §dWizard Tower
     */
    private val skyblockAreaPattern by patternGroup.pattern(
        "skyblock.area",
        "\\s*§(?<symbol>7⏣|5ф) §(?<color>.)(?<area>.*)"
    )

    private var lastIslandType = IslandType.UNKNOWN

    val isConnected: Boolean
        get() = HypixelAPI.onHypixel && HypixelAPI.gametype == "SKYBLOCK"

    var isGuesting: Boolean = false
        private set

    var island: IslandType = IslandType.UNKNOWN
        private set

    var area: String? = null
    var areaWithSymbol: String? = null

    var gamemode: Gamemode = Gamemode.UNKNOWN
        private set

    var profileName: String? = null
        private set

    var profileId: UUID? = null
        private set

    val players: Int
        get() {
            var amount = 0
            val playerPatternList = listOf(
                playerAmountPattern,
                playerAmountCoopPattern,
                playerAmountGuestingPattern
            )

            for (pattern in playerPatternList) {
                TabListData.getTabList().matchFirst(pattern) {
                    amount += group("amount").toInt()
                }
            }
            amount += TabListData.getTabList().count { soloProfileAmountPattern.matches(it) }

            return amount
        }

    val maxPlayers: Int
        get() {
            ScoreboardData.sidebarLinesFormatted.matchFirst(scoreboardVisitingAmountPattern) {
                return group("maxamount").toInt()
            }

            return when (island) {
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
        lastIslandType = IslandType.UNKNOWN
    }

    @HandleEvent
    fun onLocationChange(event: HypixelLocationEvent) {
        if (event.type != "SKYBLOCK") return reset()
        island = event.map?.let(IslandType::getByNameOrNull) ?: IslandType.UNKNOWN
        if (island == IslandType.UNKNOWN) {
            ChatUtils.debug("Unknown island detected: '${event.map}'")
        }
    }

    @HandleEvent
    fun onDisconnect(event: ClientDisconnectEvent) {
        reset()
    }

    @HandleEvent
    fun onScoreboardUpdate(event: ScoreboardUpdateEvent) {
        if (guestPattern.matches(ScoreboardData.objectiveTitle.removeColor()) != isGuesting) {
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

        if (lastIslandType != island) {
            lastIslandType = island
            IslandChangeEvent(island).post()
        }

        event.scoreboard.matchFirst(skyblockAreaPattern) {
            area = LocationFixData.fixLocation(island) ?: group("area")
            areaWithSymbol = group().trim()
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
