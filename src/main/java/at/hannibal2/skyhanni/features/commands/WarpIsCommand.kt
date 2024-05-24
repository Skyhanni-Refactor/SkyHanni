package at.hannibal2.skyhanni.features.commands

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.MessageSendToServerEvent
import at.hannibal2.skyhanni.utils.HypixelCommands

object WarpIsCommand {

    @HandleEvent(onlyOnSkyblock = true)
    fun onMessageSendToServer(event: MessageSendToServerEvent) {
        if (!SkyHanniMod.feature.misc.commands.replaceWarpIs) return

        if (event.message.lowercase() == "/warp is") {
            event.cancel()
            HypixelCommands.island()
        }
    }
}
