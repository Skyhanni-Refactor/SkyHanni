package at.hannibal2.skyhanni.events.hypixel

import at.hannibal2.skyhanni.events.LorenzEvent

class HypixelLocationEvent(
    val server: String,
    val type: String?,
    val lobby: String?,
    val mode: String?,
    val map: String?,
) : LorenzEvent()
