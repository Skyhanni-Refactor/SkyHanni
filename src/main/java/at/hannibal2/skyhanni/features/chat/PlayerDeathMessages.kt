package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.features.misc.MarkedPlayerManager
import at.hannibal2.skyhanni.features.nether.kuudra.KuudraAPI
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.mc.McWorld
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.entity.EntityOtherPlayerMP

object PlayerDeathMessages {

    private val config get() = SkyHanniMod.feature.gui.markedPlayers

    private val lastTimePlayerSeen = mutableMapOf<String, Long>()

    //§c ☠ §r§7§r§bZeroHazel§r§7 was killed by §r§8§lAshfang§r§7§r§7.
    private val deathMessagePattern by RepoPattern.pattern(
        "chat.player.death",
        "§c ☠ §r§7§r§.(?<name>.+)§r§7 (?<reason>.+)"
    )

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isHideFarDeathsEnabled()) return

        checkOtherPlayers()
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!SkyBlockAPI.isConnected) return

        val message = event.message
        deathMessagePattern.matchMatcher(message) {
            val name = group("name")
            if (config.highlightInChat &&
                !DungeonAPI.inDungeon() && !KuudraAPI.inKuudra && MarkedPlayerManager.isMarkedPlayer(name)
            ) {
                val reason = group("reason").removeColor()

                val color = config.chatColor.getChatColor()
                ChatUtils.chat(" §c☠ $color$name §7$reason", false)
                event.blockedReason = "marked_player_death"
                return
            }

            val time = System.currentTimeMillis() > lastTimePlayerSeen.getOrDefault(name, 0) + 30_000
            if (isHideFarDeathsEnabled() && time) {
                event.blockedReason = "far_away_player_death"
            }
        }
    }

    private fun checkOtherPlayers() {
        val location = LocationUtils.playerLocation()
        for (otherPlayer in McWorld.getEntitiesOf<EntityOtherPlayerMP>()
            .filter { it.getLorenzVec().distance(location) < 25 }) {
            lastTimePlayerSeen[otherPlayer.name] = System.currentTimeMillis()
        }
    }

    private fun isHideFarDeathsEnabled(): Boolean {
        return SkyBlockAPI.isConnected && SkyHanniMod.feature.chat.hideFarDeathMessages && !DungeonAPI.inDungeon() && !KuudraAPI.inKuudra
    }
}
