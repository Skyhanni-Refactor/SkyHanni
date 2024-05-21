package at.hannibal2.skyhanni.events.chat.hypixel

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.utils.ComponentSpan
import net.minecraft.util.IChatComponent

open class AbstractChatEvent(
    val authorComponent: ComponentSpan,
    val messageComponent: ComponentSpan,
    var chatComponent: IChatComponent,
    var blockedReason: String? = null,
) : SkyHanniEvent() {
    val message by lazy { messageComponent.getText() }
    val author by lazy { authorComponent.getText() }
}
