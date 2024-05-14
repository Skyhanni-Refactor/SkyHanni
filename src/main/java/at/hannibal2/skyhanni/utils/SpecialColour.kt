package at.hannibal2.skyhanni.utils

import java.awt.Color

/**
 * Taken from NotEnoughUpdates
 */
object SpecialColour {

    private const val MIN_CHROMA_SECS = 1
    private const val MAX_CHROMA_SECS = 60
    private const val RADIX = 10

    var startTime: Long = -1

    fun specialToChromaRGB(special: String): Int {
        if (startTime < 0) startTime = System.currentTimeMillis()

        val d = decomposeColourString(special)
        val chr = d[4]
        val a = d[3]
        val r = d[2]
        val g = d[1]
        val b = d[0]

        val hsv = Color.RGBtoHSB(r, g, b, null)

        if (chr > 0) {
            val seconds = getSecondsForSpeed(chr)
            hsv[0] += (System.currentTimeMillis() - startTime) / 1000f / seconds
            hsv[0] %= 1f
            if (hsv[0] < 0) hsv[0] += 1f
        }

        return (a and 0xFF) shl 24 or (Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]) and 0x00FFFFFF)
    }

    private fun decomposeColourString(colourString: String): IntArray {
        val split = colourString.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val arr = IntArray(split.size)

        for (i in split.indices) {
            arr[i] = split[split.size - 1 - i].toInt(RADIX)
        }
        return arr
    }

    fun getSpeed(special: String): Int {
        return decomposeColourString(special)[4]
    }

    private fun getSecondsForSpeed(speed: Int): Float {
        return (255 - speed) / 254f * (MAX_CHROMA_SECS - MIN_CHROMA_SECS) + MIN_CHROMA_SECS
    }

    fun rotateHue(argb: Int, degrees: Int): Int {
        val a = (argb shr 24) and 0xFF
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = (argb) and 0xFF

        val hsv = Color.RGBtoHSB(r, g, b, null)

        hsv[0] += degrees / 360f
        hsv[0] %= 1f

        return (a and 0xFF) shl 24 or (Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]) and 0x00FFFFFF)
    }
}
