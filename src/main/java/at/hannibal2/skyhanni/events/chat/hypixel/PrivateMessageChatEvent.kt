package at.hannibal2.skyhanni.events.chat.hypixel

import at.hannibal2.skyhanni.utils.ComponentSpan
import net.minecraft.util.IChatComponent

class PrivateMessageChatEvent(
    val direction: String?,
    author: ComponentSpan,
    message: ComponentSpan,
    chatComponent: IChatComponent,
    blockedReason: String? = null,
) : AbstractChatEvent(author, message, chatComponent, blockedReason)
