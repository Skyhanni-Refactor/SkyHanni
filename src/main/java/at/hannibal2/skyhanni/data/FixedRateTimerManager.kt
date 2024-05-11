package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.mc.McClient
import kotlin.concurrent.fixedRateTimer

class FixedRateTimerManager {
    private var totalSeconds = 0

    init {
        fixedRateTimer(name = "skyhanni-fixed-rate-timer-manager", period = 1000L) {
            McClient.schedule {
                if (!LorenzUtils.onHypixel) return@schedule
                SecondPassedEvent(totalSeconds).postAndCatch()
                totalSeconds++
            }
        }
    }
}
