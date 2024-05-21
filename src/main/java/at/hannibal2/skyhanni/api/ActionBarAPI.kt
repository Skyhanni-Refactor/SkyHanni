package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.ActionBarUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.utils.StringUtils.stripHypixelMessage
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ActionBarAPI {

    private const val ACTION_BAR_TYPE = 2

    var actionBar = ""
        private set

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        actionBar = ""
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onChatReceive(event: ClientChatReceivedEvent) {
        if (event.type.toInt() != ACTION_BAR_TYPE) return

        actionBar = event.message.formattedText.stripHypixelMessage()

        val actionBarEvent = ActionBarUpdateEvent(actionBar, event.message)
        actionBarEvent.post()

        if (event.message.formattedText != actionBarEvent.chatComponent.formattedText) {
            event.message = actionBarEvent.chatComponent
        }
    }
}
