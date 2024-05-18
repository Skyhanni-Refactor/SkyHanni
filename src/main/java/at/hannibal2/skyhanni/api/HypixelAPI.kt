package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.utils.mc.McPlayer

//TODO move HypixelData in here
object HypixelAPI {

    val connected: Boolean get() = HypixelData.hypixelLive || HypixelData.hypixelAlpha
    val onHypixel get() = connected && McPlayer.player != null
}
