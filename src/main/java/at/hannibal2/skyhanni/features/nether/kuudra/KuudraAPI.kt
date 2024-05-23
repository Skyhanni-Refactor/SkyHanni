package at.hannibal2.skyhanni.features.nether.kuudra

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.kuudra.KuudraCompleteEvent
import at.hannibal2.skyhanni.events.kuudra.KuudraEnterEvent
import at.hannibal2.skyhanni.events.minecraft.ClientTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

object KuudraAPI {

    private val patternGroup = RepoPattern.group("data.kuudra")

    private val tierPattern by patternGroup.pattern(
        "scoreboard.tier",
        " §7⏣ §cKuudra's Hollow §8\\(T(?<tier>.*)\\)"
    )
    private val completePattern by patternGroup.pattern(
        "chat.complete",
        "§.\\s*(?:§.)*KUUDRA DOWN!"
    )

    var kuudraTier: Int? = null
    val inKuudra: Boolean
        get() = kuudraTier != null && SkyBlockAPI.isConnected

    fun inKuudra() = kuudraTier != null

    @HandleEvent
    fun onTick(event: ClientTickEvent) {
        if (!SkyBlockAPI.isConnected) return
        if (kuudraTier != null) return
        for (line in ScoreboardData.sidebarLinesFormatted) {
            tierPattern.matchMatcher(line) {
                val tier = group("tier").toInt()
                kuudraTier = tier
                KuudraEnterEvent(tier).post()
            }
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        kuudraTier = null
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        val message = event.message
        completePattern.matchMatcher(message) {
            val tier = kuudraTier ?: return
            KuudraCompleteEvent(tier).post()
        }
    }

}
