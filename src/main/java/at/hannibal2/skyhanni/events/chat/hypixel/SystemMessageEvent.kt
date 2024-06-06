package at.hannibal2.skyhanni.events.chat.hypixel

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import net.minecraft.util.IChatComponent

class SystemMessageEvent(
    val message: String,
    var chatComponent: IChatComponent,
    var blockedReason: String? = null,
) : SkyHanniEvent()
