package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.hypixel.HypixelJoinEvent
import at.hannibal2.skyhanni.events.hypixel.HypixelLocationEvent
import at.hannibal2.skyhanni.events.minecraft.ClientDisconnectEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.mc.McPlayer
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
object HypixelAPI {

    private val lobbyTypePattern by RepoPattern.pattern(
        "hypixel.lobbytype",
        "(?<lobbyType>.*lobby)\\d+"
    )

    var connected: Boolean = false
        private set

    val onHypixel get() = connected && McPlayer.player != null

    var server: String? = null
        private set

    var gametype: String? = null
        private set

    var lobbyName: String? = null
        private set

    var lobbyType: String? = null
        private set

    var lastWorldChange: SimpleTimeMark = SimpleTimeMark.farPast()
        private set

    @HandleEvent
    fun onHypixelJoin(event: HypixelJoinEvent) {
        connected = true
    }

    @HandleEvent
    fun onDisconnect(event: ClientDisconnectEvent) {
        connected = false
    }

    @HandleEvent(priority = -1000000000)
    fun onLocationChange(event: HypixelLocationEvent) {
        server = event.server
        gametype = event.type
        lobbyName = event.lobby
        lobbyType = event.lobby?.let { lobbyTypePattern.matchMatcher(it) { groupOrNull("lobbyType") } }

        lastWorldChange = SimpleTimeMark.now()
    }
}
