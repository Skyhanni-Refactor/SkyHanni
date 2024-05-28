package at.hannibal2.skyhanni.features.commands.tabcomplete

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.FriendAPI
import at.hannibal2.skyhanni.data.PartyAPI
import at.hannibal2.skyhanni.data.jsonobjects.repo.VipVisitsJson
import at.hannibal2.skyhanni.events.utils.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.mc.McWorld

@SkyHanniModule
object PlayerTabComplete {

    private val config get() = SkyHanniMod.feature.misc.commands.tabComplete
    private var vipVisits = listOf<String>()

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<VipVisitsJson>("VipVisits")
        vipVisits = data.vipVisits
    }

    enum class PlayerCategory {
        FRIENDS,
        ISLAND_PLAYERS,
        PARTY,
    }

    fun handleTabComplete(command: String): List<String>? {
        val commands = mapOf(
            "f" to listOf(PlayerCategory.FRIENDS),
            "friend" to listOf(PlayerCategory.FRIENDS),

            "msg" to listOf(),
            "w" to listOf(),
            "tell" to listOf(),
            "boop" to listOf(),

            "visit" to listOf(),
            "invite" to listOf(),
            "ah" to listOf(),

            "pv" to listOf(), // NEU's Profile Viewer
            "shmarkplayer" to listOf(), // SkyHanni's Mark Player

            "trade" to listOf(PlayerCategory.FRIENDS, PlayerCategory.PARTY)
        )
        val ignored = commands[command] ?: return null

        return buildList {

            if (config.friends && PlayerCategory.FRIENDS !in ignored) {
                FriendAPI.getAllFriends().filter { it.bestFriend || !config.onlyBestFriends }
                    .forEach { add(it.name) }
            }

            if (config.islandPlayers && PlayerCategory.ISLAND_PLAYERS !in ignored) {
                for (entity in McWorld.players) {
                    add(entity.name)
                }
            }

            if (config.party && PlayerCategory.PARTY !in ignored) {
                for (member in PartyAPI.partyMembers) {
                    add(member)
                }
            }

            if (config.vipVisits && command == "visit") {
                for (visit in vipVisits) {
                    add(visit)
                }
            }
        }
    }
}
