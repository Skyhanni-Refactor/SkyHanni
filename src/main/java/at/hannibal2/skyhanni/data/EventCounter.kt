package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RecalculatingValue
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import kotlin.time.Duration.Companion.seconds

object EventCounter {

    private val config get() = SkyHanniMod.feature.dev.debug

    private var map = mutableMapOf<String, Int>()
    private var lastUpdate = SimpleTimeMark.now()

    private val enabled = RecalculatingValue(1.seconds) {
        HypixelAPI.onHypixel && config.eventCounter
    }

    fun count(eventName: String) {
        if (!enabled.getValue()) return

        map.addOrPut(eventName, 1)

        if (lastUpdate.passedSince() > 1.seconds) {
            lastUpdate = SimpleTimeMark.now()

            print(map)

            map.clear()
        }
    }

    private fun print(map: MutableMap<String, Int>) {
        println("")
        var total = 0
        for ((name, amount) in map.entries.sortedBy { it.value }) {
            println("$name (${amount.addSeparators()} times)")
            total += amount
        }
        println("")
        println("total: ${total.addSeparators()}")
    }
}
