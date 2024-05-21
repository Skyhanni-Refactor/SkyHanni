package at.hannibal2.skyhanni.features.gui.customscoreboard

import at.hannibal2.skyhanni.api.skyblock.Gamemode
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.config.features.gui.customscoreboard.DisplayConfig
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.features.bingo.BingoAPI
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.Companion.displayConfig
import at.hannibal2.skyhanni.utils.NumberUtil
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatDouble
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeResets
import at.hannibal2.skyhanni.utils.StringUtils.trimWhiteSpace
import java.util.regex.Pattern

object CustomScoreboardUtils {

    internal fun getGroupFromPattern(list: List<String>, pattern: Pattern, group: String) = list.map {
        it.removeResets().trimWhiteSpace()
    }.firstNotNullOfOrNull { line ->
        pattern.matchMatcher(line) {
            group(group)
        }
    } ?: "0"

    fun getProfileTypeSymbol() = when(SkyBlockAPI.gamemode) {
        Gamemode.IRONMAN -> "§7♲ "
        Gamemode.STRANDED -> "§a☀ "
        Gamemode.BINGO -> ScoreboardData.sidebarLinesFormatted.firstNotNullOfOrNull {
            BingoAPI.getIconFromScoreboard(it)?.plus(" ")
        } ?: "§e❤ "

        else -> "§e"
    }

    internal fun Number.formatNum(): String = when (displayConfig.numberFormat) {
        DisplayConfig.NumberFormat.SHORT -> NumberUtil.format(this)
        DisplayConfig.NumberFormat.LONG -> this.addSeparators()
        else -> "0"
    }

    internal fun String.formatNum() = this.formatDouble().formatNum()

    class UndetectedScoreboardLines(message: String) : Exception(message)
}
