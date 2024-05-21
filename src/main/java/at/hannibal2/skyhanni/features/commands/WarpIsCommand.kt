package at.hannibal2.skyhanni.features.commands

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.MessageSendToServerEvent
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils

object WarpIsCommand {

    @HandleEvent
    fun onMessageSendToServer(event: MessageSendToServerEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!SkyHanniMod.feature.misc.commands.replaceWarpIs) return

        if (event.message.lowercase() == "/warp is") {
            event.cancel()
            HypixelCommands.island()
        }
    }
}
