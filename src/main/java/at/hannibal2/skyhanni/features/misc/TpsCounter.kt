package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature
import at.hannibal2.skyhanni.events.render.gui.GuiRenderEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.PacketEvent
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.concurrent.fixedRateTimer

object TpsCounter {

    private val config get() = SkyHanniMod.feature.gui

    private const val MIN_DATA_AMOUNT = 5
    private const val WAIT_AFTER_WORLD_SWITCH = 6

    private var packetsFromLastSecond = 0
    private var tpsList = mutableListOf<Int>()
    private var ignoreFirstTicks = WAIT_AFTER_WORLD_SWITCH
    private var hasPacketReceived = false

    private var display = ""

    init {
        fixedRateTimer(name = "skyhanni-tps-counter-seconds", period = 1000L) {
            if (!isEnabled()) return@fixedRateTimer
            if (packetsFromLastSecond == 0) return@fixedRateTimer

            if (ignoreFirstTicks > 0) {
                ignoreFirstTicks--
                val current = ignoreFirstTicks + MIN_DATA_AMOUNT
                display = "§eTPS: §f(${current}s)"
                packetsFromLastSecond = 0
                return@fixedRateTimer
            }

            tpsList.add(packetsFromLastSecond)
            packetsFromLastSecond = 0
            if (tpsList.size > 10) {
                tpsList = tpsList.drop(1).toMutableList()
            }

            display = if (tpsList.size < MIN_DATA_AMOUNT) {
                val current = MIN_DATA_AMOUNT - tpsList.size
                "§eTPS: §f(${current}s)"
            } else {
                val sum = tpsList.sum().toDouble()
                var tps = (sum / tpsList.size).roundTo(1)
                if (tps > 20) tps = 20.0
                val color = getColor(tps)
                "§eTPS: $color$tps"
            }
        }
        // TODO use DelayedRun
        fixedRateTimer(name = "skyhanni-tps-counter-ticks", period = 50L) {
            if (!isEnabled()) return@fixedRateTimer

            if (hasPacketReceived) {
                hasPacketReceived = false
                packetsFromLastSecond++
            }
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        tpsList.clear()
        packetsFromLastSecond = 0
        ignoreFirstTicks = WAIT_AFTER_WORLD_SWITCH
        display = ""
    }

    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
    fun onPacketReceive(event: PacketEvent.ReceiveEvent) {
        if (!config.tpsDisplay) return
        hasPacketReceived = true
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return

        config.tpsDisplayPosition.renderString(display, posLabel = "Tps Display")
    }

    private fun isEnabled() = HypixelAPI.onHypixel && config.tpsDisplay &&
        (LorenzUtils.inSkyBlock || OutsideSbFeature.TPS_DISPLAY.isSelected())

    private fun getColor(tps: Double) = when {
        tps > 19.8 -> "§2"
        tps > 19 -> "§a"
        tps > 17.5 -> "§6"
        tps > 12 -> "§c"

        else -> "§4"
    }
}
