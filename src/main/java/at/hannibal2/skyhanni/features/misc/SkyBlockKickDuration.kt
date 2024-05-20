package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.render.gui.GuiRenderEvent
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.datetime.TimeUtils.format
import at.hannibal2.skyhanni.utils.mc.McSound
import at.hannibal2.skyhanni.utils.mc.McSound.play
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object SkyBlockKickDuration {

    private val config get() = SkyHanniMod.feature.misc.kickDuration

    private var kickMessage = false
    private var showTime = false
    private var lastKickTime = SimpleTimeMark.farPast()
    private var hasWarned = false

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        if (event.message == "§cYou were kicked while joining that server!") {

            if (HypixelAPI.onHypixel && !LorenzUtils.inSkyBlock) {
                kickMessage = false
                showTime = true
                lastKickTime = SimpleTimeMark.now()
            } else {
                kickMessage = true
            }
        }

        if (event.message == "§cThere was a problem joining SkyBlock, try again in a moment!") {
            kickMessage = false
            showTime = true
            lastKickTime = SimpleTimeMark.now()
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        if (!isEnabled()) return
        if (kickMessage) {
            kickMessage = false
            showTime = true
            lastKickTime = SimpleTimeMark.now()
        }
        hasWarned = false
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (!HypixelAPI.onHypixel) return
        if (!showTime) return

        if (LorenzUtils.inSkyBlock) {
            showTime = false
        }

        if (lastKickTime.passedSince() > 5.minutes) {
            showTime = false
        }

        if (lastKickTime.passedSince() > config.warnTime.get().seconds) {
            if (!hasWarned) {
                hasWarned = true
                warn()
            }
        }

        val format = lastKickTime.passedSince().format()
        config.position.renderString(
            "§cLast kicked from SkyBlock §b$format ago",
            posLabel = "SkyBlock Kick Duration"
        )
    }

    private fun warn() {
        TitleManager.sendTitle("§eTry rejoining SkyBlock now!", 3.seconds)
        McSound.BEEP.play()
    }

    fun isEnabled() = config.enabled
}
