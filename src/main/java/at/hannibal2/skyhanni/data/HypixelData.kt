package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.minecraft.ClientDisconnectEvent
import at.hannibal2.skyhanni.events.minecraft.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.TabListUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.skyblock.IslandChangeEvent
import at.hannibal2.skyhanni.events.utils.ProfileJoinEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzLogger
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TabListData
import at.hannibal2.skyhanni.utils.UtilsPatterns
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import kotlin.time.Duration.Companion.seconds

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
    private val scoreboardVisitingAmoutPattern by patternGroup.pattern(
        "scoreboard.visiting.amount",
        "\\s+§.✌ §.\\(§.(?<currentamount>\\d+)§./(?<maxamount>\\d+)\\)"
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

    var inLimbo = false
    var skyBlock = false
    var skyBlockIsland = IslandType.UNKNOWN
    var serverId: String? = null

    var profileName = ""
    var joinedWorld = SimpleTimeMark.farPast()

    var skyBlockArea: String? = null
    var skyBlockAreaWithSymbol: String? = null

    private fun checkCurrentServerId(scoreboard: List<String>) {
        if (!LorenzUtils.inSkyBlock) return
        if (serverId != null) return
        if (LorenzUtils.lastWorldSwitch.passedSince() < 1.seconds) return
        if (!TabListData.fullyLoaded) return

        scoreboard.matchFirst(serverIdScoreboardPattern) {
            val serverType = if (group("servertype") == "M") "mega" else "mini"
            serverId = "$serverType${group("serverid")}"
            return
        }

        TabListData.getTabList().matchFirst(serverIdTablistPattern) {
            serverId = group("serverid")
            return
        }

        ErrorManager.logErrorWithData(
            Exception("NoServerId"), "Could not find server id",
            "islandType" to LorenzUtils.skyBlockIsland,
            "tablist" to TabListData.getTabList(),
            "scoreboard" to ScoreboardData.sidebarLinesFormatted
        )
    }

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

    fun getMaxPlayersForCurrentServer(): Int {
        ScoreboardData.sidebarLinesFormatted.matchFirst(scoreboardVisitingAmoutPattern) {
            return group("maxamount").toInt()
        }

            return when (skyBlockIsland) {
                IslandType.MINESHAFT -> 4
                IslandType.CATACOMBS -> 5
                IslandType.CRYSTAL_HOLLOWS -> 24
                IslandType.CRIMSON_ISLE -> 24
                else -> if (serverId?.startsWith("mega") == true) 80 else 26
            }
        }

    private var loggerIslandChange = LorenzLogger("debug/island_change")

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        skyBlock = false
        inLimbo = false
        joinedWorld = SimpleTimeMark.now()
        serverId = null
        skyBlockArea = null
        skyBlockAreaWithSymbol = null
    }

    @HandleEvent
    fun onDisconnect(event: ClientDisconnectEvent) {
        skyBlock = false
        skyBlockArea = null
        skyBlockAreaWithSymbol = null
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!HypixelAPI.onHypixel) return

        val message = event.message.removeColor().lowercase()
        if (message.startsWith("your profile was changed to:")) {
            val newProfile = message.replace("your profile was changed to:", "").replace("(co-op)", "").trim()
            if (profileName == newProfile) return
            profileName = newProfile
            ProfileJoinEvent(newProfile).post()
        }
        if (message.startsWith("you are playing on profile:")) {
            val newProfile = message.replace("you are playing on profile:", "").replace("(co-op)", "").trim()
            if (profileName == newProfile) return
            profileName = newProfile
            ProfileJoinEvent(newProfile).post()
        }
    }

    @HandleEvent
    fun onTabListUpdate(event: TabListUpdateEvent) {
        event.tabList.matchFirst(UtilsPatterns.tabListProfilePattern) {
            var newProfile = group("profile").lowercase()

            // Hypixel shows the profile name reversed while in the Rift
            if (RiftAPI.inRift()) newProfile = newProfile.reversed()
            if (profileName == newProfile) return
            profileName = newProfile
            ProfileJoinEvent(newProfile).post()
        }
    }

    @HandleEvent(HandleEvent.HIGHEST)
    fun onScoreboardUpdate(event: ScoreboardUpdateEvent) {
        if (event.scoreboard.isEmpty()) return

        val inSkyblock = scoreboardTitlePattern.matches(ScoreboardData.objectiveTitle.removeColor())

        if (inSkyblock) {
            checkIsland()
            checkCurrentServerId(event.scoreboard)
        }
        skyBlock = inSkyblock
        if (!inSkyblock) return

        event.scoreboard.matchFirst(skyblockAreaPattern) {
            val originalLocation = group("area")
            skyBlockArea = LocationFixData.fixLocation(skyBlockIsland) ?: originalLocation
            skyBlockAreaWithSymbol = group().trim()
        }
        checkProfileName()
    }

    private fun checkProfileName() {
        if (profileName.isNotEmpty()) return

        TabListData.getTabList().matchFirst(UtilsPatterns.tabListProfilePattern) {
            profileName = group("profile").lowercase()
            ProfileJoinEvent(profileName).post()
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
            IslandChangeEvent(islandType, skyBlockIsland).post()
            if (islandType == IslandType.UNKNOWN) {
                ChatUtils.debug("Unknown island detected: '$foundIsland'")
                loggerIslandChange.log("Unknown: '$foundIsland'")
            } else {
                loggerIslandChange.log(islandType.name)
            }
            skyBlockIsland = islandType
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
