package at.hannibal2.skyhanni.events.skyblock

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.api.skyblock.IslandType

class IslandChangeEvent(val newIsland: IslandType) : SkyHanniEvent()
