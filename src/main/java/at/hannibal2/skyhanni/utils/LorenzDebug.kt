package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod

object LorenzDebug {

    private val logger = LorenzLogger("debug")

    fun log(text: String) {
        logger.log(text)
        SkyHanniMod.logger.info("debug logger: $text")
    }

    fun chatAndLog(text: String) {
        ChatUtils.debug(text)
        log(text)
    }
}
