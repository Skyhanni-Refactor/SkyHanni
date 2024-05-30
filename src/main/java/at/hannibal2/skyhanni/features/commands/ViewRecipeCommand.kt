package at.hannibal2.skyhanni.features.commands

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.MessageSendToServerEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.NEUItems
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ViewRecipeCommand {

    private val config get() = SkyHanniMod.feature.misc.commands

    /**
     * REGEX-TEST: /viewrecipe aspect of the end
     * REGEX-TEST: /viewrecipe aspect_of_the_end
     * REGEX-TEST: /viewrecipe ASPECT_OF_THE_END
     */
    private val pattern by RepoPattern.pattern(
        "commands.viewrecipe",
        "\\/viewrecipe (?<item>.*)"
    )

    @HandleEvent
    fun onMessageSendToServer(event: MessageSendToServerEvent) {
        if (!config.viewRecipeLowerCase) return
        if (event.senderIsSkyhanni()) return

        val item = pattern.matchMatcher(event.message.lowercase()) {
            group("item").uppercase().replace(" ", "_")
        } ?: return

        event.cancel()
        HypixelCommands.viewRecipe(item)
    }

    val list by lazy {
        val list = mutableListOf<String>()
        for ((key, value) in NEUItems.allNeuRepoItems()) {
            if (value.has("recipe")) {
                list.add(key.lowercase())
            }
        }
        list
    }

    fun customTabComplete(command: String): List<String>? {
        if (command == "viewrecipe" && config.tabComplete.viewrecipeItems) {
            return list
        }

        return null
    }
}
