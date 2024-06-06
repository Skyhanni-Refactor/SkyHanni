package at.hannibal2.skyhanni.events.minecraft

import at.hannibal2.skyhanni.api.event.SkyHanniEvent

class ScoreboardUpdateEvent(val scoreboard: List<String>) : SkyHanniEvent()
