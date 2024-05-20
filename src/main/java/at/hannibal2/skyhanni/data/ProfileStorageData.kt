package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.SackData
import at.hannibal2.skyhanni.config.storage.PlayerSpecificStorage
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.TabListUpdateEvent
import at.hannibal2.skyhanni.events.hypixel.HypixelJoinEvent
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TabListData
import at.hannibal2.skyhanni.utils.UtilsPatterns
import at.hannibal2.skyhanni.utils.mc.McPlayer
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

object ProfileStorageData {

    var playerSpecific: PlayerSpecificStorage? = null
    var profileSpecific: ProfileSpecificStorage? = null
    var loaded = false
    private var noTabListTime = SimpleTimeMark.farPast()

    private var sackPlayers: SackData.PlayerSpecific? = null
    var sackProfiles: SackData.ProfileSpecific? = null

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onProfileJoin(event: ProfileJoinEvent) {
        val playerSpecific = playerSpecific
        val sackPlayers = sackPlayers
        if (playerSpecific == null) {
            ErrorManager.skyHanniError("playerSpecific is null in ProfileJoinEvent! If you have a mod that auto-joins SkyBlock, disable it!")
        }
        if (sackPlayers == null) {
            ErrorManager.skyHanniError("sackPlayers is null in ProfileJoinEvent!")
        }

        val profileName = event.name
        loadProfileSpecific(playerSpecific, sackPlayers, profileName)
        ConfigLoadEvent().postAndCatch()
    }

    @HandleEvent
    fun onTabListUpdate(event: TabListUpdateEvent) {
        if (!LorenzUtils.inSkyBlock) return

        event.tabList.matchFirst(UtilsPatterns.tabListProfilePattern) {
            noTabListTime = SimpleTimeMark.farPast()
            return
        }

        noTabListTime = SimpleTimeMark.now()
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (noTabListTime == SimpleTimeMark.farPast()) return

        if (noTabListTime.passedSince() > 3.seconds) {
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
        ConfigLoadEvent().postAndCatch()
    }

    @HandleEvent
    fun onHypixelJoin(event: HypixelJoinEvent) {
        playerSpecific = SkyHanniMod.feature.storage.players.getOrPut(McPlayer.uuid) { PlayerSpecificStorage() }
        sackPlayers = SkyHanniMod.sackData.players.getOrPut(McPlayer.uuid) { SackData.PlayerSpecific() }
        ConfigLoadEvent().postAndCatch()
    }
}
