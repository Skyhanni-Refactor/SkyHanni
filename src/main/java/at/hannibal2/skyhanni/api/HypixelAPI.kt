package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.events.hypixel.HypixelLocationEvent
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.mc.McPlayer

//TODO move HypixelData in here
object HypixelAPI {

    private val lobbyTypePattern by HypixelData.patternGroup.pattern(
        "lobbytype",
        "(?<lobbyType>.*lobby)\\d+"
    )

    val connected: Boolean get() = HypixelData.hypixelLive || HypixelData.hypixelAlpha
    val onHypixel get() = connected && McPlayer.player != null

    var lobbyName: String? = null
        private set

    var lobbyType: String? = null
        private set

    @HandleEvent
    fun onLocationChange(event: HypixelLocationEvent) {
        lobbyName = event.lobby
        lobbyType = event.lobby?.let { lobbyTypePattern.matchMatcher(it) { groupOrNull("lobbyType") } }
    }
}
