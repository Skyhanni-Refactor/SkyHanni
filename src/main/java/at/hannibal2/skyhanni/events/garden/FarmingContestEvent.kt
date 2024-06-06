package at.hannibal2.skyhanni.events.garden

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.contest.FarmingContestPhase

class FarmingContestEvent(val crop: CropType, val contestPhase: FarmingContestPhase) : SkyHanniEvent()
