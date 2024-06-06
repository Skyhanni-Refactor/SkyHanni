package at.hannibal2.skyhanni.events.chat

import at.hannibal2.skyhanni.api.event.CancellableSkyHanniEvent
import at.hannibal2.skyhanni.utils.system.ModInstance

class MessageSendToServerEvent(
    val message: String,
    val splitMessage: List<String>,
    val originatingModContainer: ModInstance?
) : CancellableSkyHanniEvent()
