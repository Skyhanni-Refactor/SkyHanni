package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandArea
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

object ArachneChatMessageHider {

    private val config get() = SkyHanniMod.feature.chat
    private var hideArachneDeadMessage = false

    private val patternGroup = RepoPattern.group("chat.arachne")
    private val arachneCallingPattern by patternGroup.pattern(
        "calling",
        "§4☄ §r.* §r§eplaced an §r§9Arachne's Calling§r§e!.*"
    )
    private val arachneCrystalPattern by patternGroup.pattern(
        "crystal",
        "§4☄ §r.* §r§eplaced an Arachne Crystal! Something is awakening!"
    )
    private val arachneSpawnPattern by patternGroup.pattern(
        "spawn",
        "§c\\[BOSS] Arachne§r§f: (?:The Era of Spiders begins now\\.|Ahhhh\\.\\.\\.A Calling\\.\\.\\.)"
    )
    // TODO more repo patterns

    @HandleEvent(onlyOnIsland = IslandType.SPIDER_DEN)
    fun onChat(event: SkyHanniChatEvent) {
        if (!isEnabled()) return
        if (IslandArea.ARACHNE_SANCTUARY.isInside()) return

        if (shouldHide(event.message)) {
            event.blockedReason = "arachne"
        }
    }

    private fun shouldHide(message: String): Boolean {

        arachneCallingPattern.matchMatcher(message) {
            return true
        }
        arachneCrystalPattern.matchMatcher(message) {
            return true
        }

        arachneSpawnPattern.matchMatcher(message) {
            return true
        }

        if (message == "§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬") {
            hideArachneDeadMessage = !hideArachneDeadMessage
            return true
        }
        if (message == "                              §r§6§lARACHNE DOWN!") {
            hideArachneDeadMessage = true
        }
        return hideArachneDeadMessage
    }

    fun isEnabled() = config.hideArachneMessages
}
