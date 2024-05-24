package at.hannibal2.skyhanni.features.event.spook

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.render.gui.GuiOverlayRenderEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.TabListData
import at.hannibal2.skyhanni.utils.mc.McSound
import at.hannibal2.skyhanni.utils.mc.McSound.play

object TheGreatSpook {

    // §r§cPrimal Fears§r§7: §r§6§lREADY!!
    private val config get() = SkyHanniMod.feature.event.spook
    private var displayTimer = ""
    private var displayFearStat = ""
    private var displayTimeLeft = ""
    private var notificationSeconds = 0

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (isAllDisabled()) return

        if (isTimerEnabled() || isNotificationEnabled()) displayTimer = checkTabList(" §r§cPrimal Fears§r§7: ")
        if (isFearStatEnabled()) displayFearStat = checkTabList(" §r§5Fear: ")
        if (isTimeLeftEnabled()) displayTimeLeft = checkTabList(" §r§dEnds In§r§7: ")
        if (isNotificationEnabled()) {
            if (displayTimer.endsWith("READY!!")) {
                if (notificationSeconds > 0) {
                    McSound.BEEP.play()
                    notificationSeconds--
                }
            } else if (displayTimer.isNotEmpty()) {
                notificationSeconds = 5
            }
        }
    }

    private fun checkTabList(matchString: String): String {
        return (TabListData.getTabList().find { it.contains(matchString) } ?: "").trim()
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiOverlayRenderEvent) {
        if (isTimerEnabled()) config.positionTimer.renderString(displayTimer, posLabel = "Primal Fear Timer")
        if (isFearStatEnabled()) config.positionFear.renderString(displayFearStat, posLabel = "Fear Stat Display")
        if (isTimeLeftEnabled()) config.positionTimeLeft.renderString(displayTimeLeft, posLabel = "Time Left Display")
    }

    private fun isTimerEnabled(): Boolean = SkyBlockAPI.isConnected && config.primalFearTimer

    private fun isNotificationEnabled(): Boolean = SkyBlockAPI.isConnected && config.primalFearNotification
    private fun isFearStatEnabled(): Boolean = SkyBlockAPI.isConnected && config.fearStatDisplay
    private fun isTimeLeftEnabled(): Boolean = SkyBlockAPI.isConnected && config.greatSpookTimeLeft

    private fun isAllDisabled(): Boolean = !isTimeLeftEnabled() && !isTimerEnabled() && !isFearStatEnabled() &&
        !isNotificationEnabled()
}
