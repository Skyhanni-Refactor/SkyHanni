package at.hannibal2.skyhanni.features.event.winter

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.render.gui.GuiOverlayRenderEvent
import at.hannibal2.skyhanni.events.utils.ProfileJoinEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.addItemIcon
import at.hannibal2.skyhanni.utils.RenderUtils.renderSingleLineWithItems
import at.hannibal2.skyhanni.utils.datetime.TimeUtils.format
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object JyrreTimer {

    private val config get() = SkyHanniMod.feature.event.winter.jyrreTimer
    private val drankBottlePattern by RepoPattern.pattern(
        "event.winter.drank.jyrre",
        "§aYou drank a §r§6Refined Bottle of Jyrre §r§aand gained §r§b\\+300✎ Intelligence §r§afor §r§b60 minutes§r§a!"
    )
    private var display = emptyList<Any>()
    private var duration = 0.seconds

    @HandleEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        resetDisplay()
    }

    private fun resetDisplay() {
        if (display.isEmpty()) return
        display = if (config.showInactive) drawDisplay() else emptyList()
        duration = 0.seconds
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!isEnabled() || !drankBottlePattern.matches(event.message)) return
        duration = 60.minutes
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        config.pos.renderSingleLineWithItems(display, posLabel = "Refined Jyrre Timer")
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return

        if (display.isNotEmpty() && !config.showInactive && duration <= 0.seconds) {
            resetDisplay()
            return
        }

        display = drawDisplay()
    }

    private val displayIcon by lazy { SkyhanniItems.REFINED_BOTTLE_OF_JYRRE().getItemStack() }

    fun drawDisplay(): MutableList<Any> {
        duration -= 1.seconds

        return mutableListOf<Any>().apply {
            addItemIcon(displayIcon)
            add("§aJyrre Boost: ")

            if (duration <= 0.seconds && config.showInactive) {
                add("§cInactive!")
            } else {
                val format = duration.format()
                add("§b$format")
            }
        }
    }

    private fun isEnabled() = SkyBlockAPI.isConnected && config.enabled
}
