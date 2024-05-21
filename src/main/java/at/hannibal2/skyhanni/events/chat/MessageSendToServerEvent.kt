package at.hannibal2.skyhanni.events.chat

import at.hannibal2.skyhanni.api.event.CancellableSkyHanniEvent
import net.minecraftforge.fml.common.ModContainer

class MessageSendToServerEvent(
    val message: String,
    val splitMessage: List<String>,
    val originatingModContainer: ModContainer?
) : CancellableSkyHanniEvent()
