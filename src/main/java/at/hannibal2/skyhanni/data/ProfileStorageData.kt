package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.SackData
import at.hannibal2.skyhanni.config.storage.PlayerSpecificStorage
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.events.hypixel.HypixelJoinEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.minecraft.TabListUpdateEvent
import at.hannibal2.skyhanni.events.utils.ConfigLoadEvent
import at.hannibal2.skyhanni.events.utils.ProfileJoinEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TabListData
import at.hannibal2.skyhanni.utils.UtilsPatterns
import at.hannibal2.skyhanni.utils.mc.McPlayer
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ProfileStorageData {

    var playerSpecific: PlayerSpecificStorage? = null
    var profileSpecific: ProfileSpecificStorage? = null
    var loaded = false
    private var noTabListTime = SimpleTimeMark.farPast()

    private var sackPlayers: SackData.PlayerSpecific? = null
    var sackProfiles: SackData.ProfileSpecific? = null

    @HandleEvent(priority = HandleEvent.HIGHEST)
    fun onProfileJoin(event: ProfileJoinEvent) {
        val playerSpecific = playerSpecific
        val sackPlayers = sackPlayers
        val profileName = event.name
        if (playerSpecific == null) {
            ErrorManager.skyHanniError("playerSpecific is null in ProfileJoinEvent!")
        }
        if (sackPlayers == null) {
            ErrorManager.skyHanniError("sackPlayers is null in ProfileJoinEvent!")
        }

        loadProfileSpecific(playerSpecific, sackPlayers, profileName)
        ConfigLoadEvent().post()
    }

    private fun workaroundIn10SecondsProfileStorage(profileName: String) {
        println("workaroundIn10SecondsProfileStorage")
        val playerSpecific = playerSpecific
        val sackPlayers = sackPlayers

        if (playerSpecific == null) {
            ErrorManager.skyHanniError(
                "failed to load your profile data delayed ",
                "onHypixel" to LorenzUtils.onHypixel,
                "HypixelData.hypixelLive" to HypixelData.hypixelLive,
                "HypixelData.hypixelAlpha" to HypixelData.hypixelAlpha,
                "sidebarLinesFormatted" to ScoreboardData.sidebarLinesFormatted,
            )
        }
        if (sackPlayers == null) {
            ErrorManager.skyHanniError("sackPlayers is null in ProfileJoinEvent!")
        }
        loadProfileSpecific(playerSpecific, sackPlayers, profileName)
        ConfigLoadEvent().post()
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onTabListUpdate(event: TabListUpdateEvent) {
        event.tabList.matchFirst(UtilsPatterns.tabListProfilePattern) {
            noTabListTime = SimpleTimeMark.farPast()
            return
        }

        noTabListTime = SimpleTimeMark.now()
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onTick(event: ClientTickEvent) {
        if (noTabListTime.isFarPast()) return

        if (noTabListTime.passedSince() > 6.seconds) {
            noTabListTime = SimpleTimeMark.now()
            val foundSkyBlockTabList = TabListData.getTabList().any { it.contains("§b§lArea:") }
            if (foundSkyBlockTabList) {
                ChatUtils.clickableChat(
                    "§cCan not read profile name from tab list! Open /widget and enable Profile Widget. " +
                        "This is needed for the mod to function! And therefore this warning cannot be disabled",
                    onClick = {
                        HypixelCommands.widget()
                    }
                )
            } else {
                ChatUtils.chat(
                    "§cExtra Information from Tab list not found! " +
                        "Enable it: SkyBlock Menu ➜ Settings ➜ Personal ➜ User Interface ➜ Player List Info"
                )
            }
        }
    }

    private fun loadProfileSpecific(
        playerSpecific: PlayerSpecificStorage,
        sackProfile: SackData.PlayerSpecific,
        profileName: String,
    ) {
        noTabListTime = SimpleTimeMark.farPast()
        profileSpecific = playerSpecific.profiles.getOrPut(profileName) { ProfileSpecificStorage() }
        sackProfiles = sackProfile.profiles.getOrPut(profileName) { SackData.ProfileSpecific() }
        loaded = true
        ConfigLoadEvent().post()
    }

    @HandleEvent
    fun onHypixelJoin(event: HypixelJoinEvent) {
        playerSpecific = SkyHanniMod.feature.storage.players.getOrPut(McPlayer.uuid) { PlayerSpecificStorage() }
        sackPlayers = SkyHanniMod.sackData.players.getOrPut(McPlayer.uuid) { SackData.PlayerSpecific() }
        ConfigLoadEvent().post()
    }
}
