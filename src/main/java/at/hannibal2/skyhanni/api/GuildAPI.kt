package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.utils.StringUtils.cleanPlayerName
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object GuildAPI {

    private val storage: MutableList<String>?
        get() = ProfileStorageData.playerSpecific?.guildMembers

    private var inGuildMessage = false
    private val list = mutableListOf<String>()

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        val message = event.message
        if (message.startsWith("§6Guild Name: ")) {
            inGuildMessage = true
            list.clear()
        } else if (message.startsWith("§eTotal Members: ")) {
            inGuildMessage = false
            storage?.clear()
            storage?.addAll(list)
            list.clear()
        } else if (inGuildMessage) {
            if (message.contains("●")) {
                for (word in message.split("●")) {
                    list.add(word.cleanPlayerName())
                }
            }
        }
    }

    fun isInGuild(name: String) = storage?.contains(name) ?: false
}
