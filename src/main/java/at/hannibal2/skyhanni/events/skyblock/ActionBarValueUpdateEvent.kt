package at.hannibal2.skyhanni.events.skyblock

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.data.ActionBarStatsData

class ActionBarValueUpdateEvent(val updated: ActionBarStatsData) : SkyHanniEvent()
