package at.hannibal2.skyhanni.utils.datetime

import at.hannibal2.skyhanni.SkyHanniMod
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId

object DateUtils {

    private const val ZONE = "UTC"

    val now: LocalDate get() = LocalDate.now(ZoneId.of(ZONE))

    @JvmStatic
    fun isDecember() = now.month == Month.DECEMBER

    @JvmStatic
    fun isAprilFools() = when {
        SkyHanniMod.feature.dev.debug.alwaysFunnyTime -> true
        SkyHanniMod.feature.dev.debug.neverFunnyTime -> false
        else -> now.month == Month.APRIL && now.dayOfMonth == 1
    }
}
