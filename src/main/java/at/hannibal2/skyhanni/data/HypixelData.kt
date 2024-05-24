package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.minecraft.ClientDisconnectEvent
import at.hannibal2.skyhanni.events.minecraft.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.TabListData
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

object HypixelData {

    val patternGroup = RepoPattern.group("data.hypixeldata")
    val islandNamePattern by patternGroup.pattern(
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

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        skyBlockArea = null
        skyBlockAreaWithSymbol = null
    }

    @HandleEvent
    fun onDisconnect(event: ClientDisconnectEvent) {
        skyBlockArea = null
        skyBlockAreaWithSymbol = null
    }

    @HandleEvent(priority = HandleEvent.HIGHEST)
    fun onScoreboardUpdate(event: ScoreboardUpdateEvent) {
        if (event.scoreboard.isEmpty()) return

        if (!SkyBlockAPI.isConnected) return
        event.scoreboard.matchFirst(skyblockAreaPattern) {
            val originalLocation = group("area")
            skyBlockArea = LocationFixData.fixLocation(skyBlockIsland) ?: originalLocation
            skyBlockAreaWithSymbol = group().trim()
        }
    }
}
