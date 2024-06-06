package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.events.chat.MessageSendToServerEvent
import at.hannibal2.skyhanni.events.minecraft.KeyPressEvent
import at.hannibal2.skyhanni.features.misc.LockMouseLook
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.mc.McScreen
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GardenWarpCommands {

    private val config get() = GardenAPI.config.gardenCommands

    private val tpPlotPattern by RepoPattern.pattern(
        "garden.warpcommand.tpplot",
        "/tp (?<plot>.*)"
    )

    private var lastWarpTime = SimpleTimeMark.farPast()

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onMessageSendToServer(event: MessageSendToServerEvent) {
        if (!config.warpCommands) return

        val message = event.message.lowercase()

        if (message == "/home") {
            event.cancel()
            HypixelCommands.warp("garden")
            ChatUtils.chat("§aTeleported you to the spawn location!", prefix = false)
        }

        if (message == "/barn") {
            event.cancel()
            HypixelCommands.teleportToPlot("barn")
            LockMouseLook.autoDisable()
        }

        tpPlotPattern.matchMatcher(event.message) {
            event.cancel()
            val plotName = group("plot")
            HypixelCommands.teleportToPlot(plotName)
            LockMouseLook.autoDisable()
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onKeyClick(event: KeyPressEvent) {
        if (McScreen.isOpen) return
        if (NEUItems.neuHasFocus()) return

        if (lastWarpTime.passedSince() < 2.seconds) return

        when (event.keyCode) {
            config.homeHotkey -> {
                HypixelCommands.warp("garden")
            }

            config.sethomeHotkey -> {
                HypixelCommands.setHome()
            }

            config.barnHotkey -> {
                LockMouseLook.autoDisable()
                HypixelCommands.teleportToPlot("barn")
            }

            else -> return
        }
        lastWarpTime = SimpleTimeMark.now()
    }
}
