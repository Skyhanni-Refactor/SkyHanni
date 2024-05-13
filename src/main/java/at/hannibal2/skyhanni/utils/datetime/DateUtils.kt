package at.hannibal2.skyhanni.utils.datetime

import java.time.Month

object DateUtils {
    fun isDecember() = TimeUtils.getCurrentLocalDate().month == Month.DECEMBER
}
