package at.hannibal2.skyhanni.features.event.anniversary

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.render.gui.GuiOverlayRenderEvent
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.RenderUtils.renderSingleLineWithItems
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.datetime.SkyBlockTime
import at.hannibal2.skyhanni.utils.datetime.TimeUtils.format
import at.hannibal2.skyhanni.utils.mc.McSound
import at.hannibal2.skyhanni.utils.mc.McSound.play
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object Year300RaffleEvent {

    private val config get() = SkyHanniMod.feature.event.century
    val displayItem by lazy { NEUItems.getItemStackOrNull("EPOCH_CAKE_ORANGE") ?: ItemStack(Items.clock) }

    private var lastTimerReceived = SimpleTimeMark.farPast()
    private var lastTimeAlerted = SimpleTimeMark.farPast()

    private var overlay: List<Any>? = null

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (event.message == "§6§lACTIVE PLAYER! §eYou gained §b+1 Raffle Ticket§e!") {
            lastTimerReceived = SimpleTimeMark.now()
        }
    }

    fun isEnabled() = SkyBlockAPI.isConnected && config.enableActiveTimer &&
        Instant.now().isBefore(SkyBlockTime(301).toInstant())

    @HandleEvent
    fun onRenderOverlay(event: GuiOverlayRenderEvent) {
        config.activeTimerPosition.renderSingleLineWithItems(
            overlay ?: return,
            posLabel = "300þ Anniversary Active Timer"
        )
    }

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!isEnabled()) {
            overlay = null
            return
        }
        val p = lastTimerReceived.passedSince()
        val timeLeft = if (p > 20.minutes) {
            0.seconds
        } else {
            20.minutes - p
        }
        if (p.isFinite() && timeLeft < 1.seconds && lastTimeAlerted.passedSince() > 5.minutes && config.enableActiveAlert) {
            McSound.CENTURY_ACTIVE_TIMER_ALERT.play()
            lastTimeAlerted = SimpleTimeMark.now()
        }
        overlay = listOf(
            Renderable.itemStack(displayItem),
            Renderable.string("§eTime Left: ${timeLeft.format()}")
        )
    }
}
