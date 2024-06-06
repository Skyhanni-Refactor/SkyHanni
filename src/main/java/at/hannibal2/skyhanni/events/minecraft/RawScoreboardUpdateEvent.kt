package at.hannibal2.skyhanni.events.minecraft

import at.hannibal2.skyhanni.api.event.SkyHanniEvent

class RawScoreboardUpdateEvent(val newList: List<String>) : SkyHanniEvent()
