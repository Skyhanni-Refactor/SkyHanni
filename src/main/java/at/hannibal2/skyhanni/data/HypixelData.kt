package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.ClientDisconnectEvent
import at.hannibal2.skyhanni.events.minecraft.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.skyblock.IslandChangeEvent
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzLogger
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TabListData
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

object HypixelData {

    val patternGroup = RepoPattern.group("data.hypixeldata")
    private val islandNamePattern by patternGroup.pattern(
        "islandname",
        "(?:§.)*(Area|Dungeon): (?:§.)*(?<island>.*)"
    )

    private val serverIdScoreboardPattern by patternGroup.pattern(
        "serverid.scoreboard",
        "§7\\d+/\\d+/\\d+ §8(?<servertype>[mM])(?<serverid>\\S+).*"
    )
    private val serverIdTablistPattern by patternGroup.pattern(
        "serverid.tablist",
        " Server: §r§8(?<serverid>\\S+)"
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
    val guestPattern by patternGroup.pattern(
        "guesting.scoreboard",
        "SKYBLOCK GUEST"
    )
    private val scoreboardTitlePattern by patternGroup.pattern(
        "scoreboard.title",
        "SK[YI]BLOCK(?: CO-OP| GUEST)?"
    )

    /**
     * REGEX-TEST:  §7⏣ §bVillage
     * REGEX-TEST:  §5ф §dWizard Tower
     */
    private val skyblockAreaPattern by patternGroup.pattern(
        "skyblock.area",
        "\\s*§(?<symbol>7⏣|5ф) §(?<color>.)(?<area>.*)"
    )

    var skyBlock = false
    var skyBlockIsland = IslandType.UNKNOWN

    var skyBlockArea: String? = null
    var skyBlockAreaWithSymbol: String? = null

    fun getPlayersOnCurrentServer(): Int {
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

    private var loggerIslandChange = LorenzLogger("debug/island_change")

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        skyBlock = false
        skyBlockArea = null
        skyBlockAreaWithSymbol = null
    }

    @HandleEvent
    fun onDisconnect(event: ClientDisconnectEvent) {
        skyBlock = false
        skyBlockArea = null
        skyBlockAreaWithSymbol = null
    }

    @HandleEvent(priority = HandleEvent.HIGHEST)
    fun onScoreboardUpdate(event: ScoreboardUpdateEvent) {
        if (event.scoreboard.isEmpty()) return

        val inSkyblock = scoreboardTitlePattern.matches(ScoreboardData.objectiveTitle.removeColor())

        if (inSkyblock) {
            checkIsland()
        }
        skyBlock = inSkyblock
        if (!inSkyblock) return

        event.scoreboard.matchFirst(skyblockAreaPattern) {
            val originalLocation = group("area")
            skyBlockArea = LocationFixData.fixLocation(skyBlockIsland) ?: originalLocation
            skyBlockAreaWithSymbol = group().trim()
        }
    }

    private fun checkIsland() {
        var foundIsland = ""
        TabListData.fullyLoaded = false

        TabListData.getTabList().matchFirst(islandNamePattern) {
            foundIsland = group("island").removeColor()
            TabListData.fullyLoaded = true
        }

        val guesting = guestPattern.matches(ScoreboardData.objectiveTitle.removeColor())
        val islandType = getIslandType(foundIsland, guesting)
        if (skyBlockIsland != islandType) {
            skyBlockIsland = islandType
            IslandChangeEvent(islandType, skyBlockIsland).post()
            if (islandType == IslandType.UNKNOWN) {
                ChatUtils.debug("Unknown island detected: '$foundIsland'")
                loggerIslandChange.log("Unknown: '$foundIsland'")
            } else {
                loggerIslandChange.log(islandType.name)
            }
        }
    }

    private fun getIslandType(name: String, guesting: Boolean): IslandType {
        val islandType = IslandType.getByNameOrUnknown(name)
        if (guesting) {
            if (islandType == IslandType.PRIVATE_ISLAND) return IslandType.PRIVATE_ISLAND_GUEST
            if (islandType == IslandType.GARDEN) return IslandType.GARDEN_GUEST
        }
        return islandType
    }
}
