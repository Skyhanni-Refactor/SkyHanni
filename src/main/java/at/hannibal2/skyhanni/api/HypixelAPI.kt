package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.events.hypixel.HypixelLocationEvent
import at.hannibal2.skyhanni.utils.mc.McPlayer

//TODO move HypixelData in here
object HypixelAPI {

    val connected: Boolean get() = HypixelData.hypixelLive || HypixelData.hypixelAlpha
    val onHypixel get() = connected && McPlayer.player != null

    @HandleEvent
    fun onLocationChange(event: HypixelLocationEvent) {
        println(event)
    }
}
