package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils.transformIf
import at.hannibal2.skyhanni.utils.StringUtils.getPlayerNameFromChatMessage
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.http.GoogleTranslator
import at.hannibal2.skyhanni.utils.system.OS
import kotlinx.coroutines.launch
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle

// TODO split into two classes: TranslatorCommand and GoogleTranslator. only communicates via getTranslationFromEnglish and getTranslationToEnglish
@SkyHanniModule
object Translator {

    private val config get() = SkyHanniMod.feature.chat

    //TODO ???????????????????
    private val messageContentRegex = Regex(".*: (.*)")

    // Logic for listening for a user click on a chat message is from NotEnoughUpdates

    @HandleEvent(priority = HandleEvent.LOWEST)
    fun onChat(event: SkyHanniChatEvent) {
        if (!isEnabled()) return

        val message = event.message
        // TODO use PlayerAllChatEvent and other player chat events
        if (message.getPlayerNameFromChatMessage() == null) return

        val editedComponent = event.chatComponent.transformIf({ siblings.isNotEmpty() }) { siblings.last() }
        if (editedComponent.chatStyle?.chatClickEvent?.action == ClickEvent.Action.OPEN_URL) return

        val clickStyle = createClickStyle(message, editedComponent.chatStyle)
        editedComponent.setChatStyle(clickStyle)
    }

    private fun createClickStyle(message: String, style: ChatStyle): ChatStyle {
        val text = messageContentRegex.find(message)!!.groupValues[1].removeColor()
        style.setChatClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/shtranslate $text"))
        style.setChatHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText("§bClick to translate!")))
        return style
    }

    fun toEnglish(args: Array<String>) {
        val message = args.joinToString(" ").removeColor()

        SkyHanniMod.coroutineScope.launch {
            GoogleTranslator.translate(message, "auto", "en").fold(
                { ChatUtils.chat("Found translation: §f${it.text}") },
                {
                    if (it is GoogleTranslator.SameLanguageError) {
                        ChatUtils.userError("The source and target languages are the same (${it.lang})")
                    } else {
                        ChatUtils.userError("Unable to translate message, an error occurred: ${it.message}")
                    }
                },
            )
        }
    }

    fun fromEnglish(args: Array<String>) {
        if (args.size < 2 || args[0].length != 2) { // args[0] is the language code
            ChatUtils.userError("Usage: /shcopytranslation <two letter language code (at the end of a translation)> <message>")
            return
        }
        val language = args[0]
        val message = args.drop(1).joinToString(" ")

        SkyHanniMod.coroutineScope.launch {
            GoogleTranslator.translate(message, "en", language).fold(
                {
                    ChatUtils.chat("Copied translation to clipboard: §f${it.text}")
                    OS.copyToClipboard(it.text)
                },
                {
                    if (it is GoogleTranslator.SameLanguageError) {
                        ChatUtils.userError("Could not translate message, the source and target languages are the same (${it.lang})")
                    } else {
                        ChatUtils.userError("Unable to translate message, an error occurred: ${it.message}")
                    }
                },
            )
        }
    }

    fun isEnabled() = config.translator
}
