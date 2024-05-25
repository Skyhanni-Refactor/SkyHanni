package at.hannibal2.skyhanni.utils

import java.awt.Color
import kotlin.math.max

object ColourUtils {

    /** transfer string colors from the config to java.awt.Color */
    fun String.toChromaColour() = Color(toChromaColourInt(), true)
    fun String.toChromaColourInt() = SpecialColour.specialToChromaRGB(this)

    fun String.getFirstColorCode() = this.takeIf { it.firstOrNull() == '§' }?.getOrNull(1)

    fun blendRGB(start: Color, end: Color, percent: Double) = Color(
        (start.red * (1 - percent) + end.red * percent).toInt(),
        (start.green * (1 - percent) + end.green * percent).toInt(),
        (start.blue * (1 - percent) + end.blue * percent).toInt()
    )

    fun Color.darker(factor: Double): Color {
        return Color(
            max((red * factor).toInt(), 0),
            max((green * factor).toInt(), 0),
            max((blue * factor).toInt(), 0),
            alpha
        )
    }

    fun Color.withAlpha(alpha: Int): Int = (alpha.coerceIn(0, 255) shl 24) or (this.rgb and 0x00ffffff)
}
