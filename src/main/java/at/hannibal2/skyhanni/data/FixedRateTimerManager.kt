package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.utils.mc.McClient
import kotlin.concurrent.fixedRateTimer

object FixedRateTimerManager {
    private var totalSeconds = 0

    init {
        fixedRateTimer(name = "skyhanni-fixed-rate-timer-manager", period = 1000L) {
            McClient.schedule {
                if (!HypixelAPI.onHypixel) return@schedule
                SecondPassedEvent(totalSeconds).post()
                totalSeconds++
            }
        }
    }
}
