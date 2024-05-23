package at.hannibal2.skyhanni.features.commands

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.HypixelAPI
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.FriendAPI
import at.hannibal2.skyhanni.data.PartyAPI
import at.hannibal2.skyhanni.events.chat.MessageSendToServerEvent
import at.hannibal2.skyhanni.events.utils.ConfigFixEvent
import at.hannibal2.skyhanni.features.misc.limbo.LimboTimeTracker
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.mc.McWorld

object PartyCommands {

    private val config get() = SkyHanniMod.feature.misc.commands

    fun kickOffline() {
        if (!config.shortCommands) return
        if (PartyAPI.partyMembers.isEmpty()) return
        HypixelCommands.partyKickOffline()
    }

    fun disband() {
        if (!config.shortCommands) return
        if (PartyAPI.partyMembers.isEmpty()) return
        HypixelCommands.partyDisband()
    }

    fun warp() {
        if (!config.shortCommands) return
        if (PartyAPI.partyMembers.isEmpty()) return
        HypixelCommands.partyWarp()
    }

    fun kick(args: Array<String>) {
        if (!config.shortCommands) return
        if (PartyAPI.partyMembers.isEmpty()) return
        if (args.isEmpty()) return
        val kickedPlayer = args[0]
        val kickedReason = args.drop(1).joinToString(" ").trim()
        if (kickedReason.isNotEmpty() && config.partyKickReason) {
            HypixelCommands.partyChat("Kicking $kickedPlayer: $kickedReason")
        }
        HypixelCommands.partyKick(kickedPlayer)
    }

    fun transfer(args: Array<String>) {
        if (args.isEmpty()) {
            if (HypixelAPI.server == "limbo") {
                LimboTimeTracker.printStats(true)
                return
            }
            HypixelCommands.playtime()
            return
        }
        if (!config.shortCommands) return
        if (PartyAPI.partyMembers.isEmpty()) return
        HypixelCommands.partyTransfer(args[0])
    }

    fun promote(args: Array<String>) {
        if (!config.shortCommands) return
        if (PartyAPI.partyMembers.isEmpty()) return
        if (args.isEmpty()) return
        HypixelCommands.partyPromote(args[0])
    }

    @HandleEvent
    fun onMessageSendToServer(event: MessageSendToServerEvent) {
        if (!config.partyKickReason) {
            return
        }
        if (!event.message.startsWith("/party kick ", ignoreCase = true)
            && !event.message.startsWith("/p kick ", ignoreCase = true)
        ) {
            return
        }
        val args = event.message.substringAfter("kick").trim().split(" ")
        if (args.isEmpty()) return
        val kickedPlayer = args[0]
        val kickReason = args.drop(1).joinToString(" ").trim()
        if (kickReason.isEmpty()) return
        event.cancel()
        HypixelCommands.partyChat("Kicking $kickedPlayer: $kickReason")
        HypixelCommands.partyKick(kickedPlayer)
    }

    fun customTabComplete(command: String): List<String>? {
        if (command == "pk" || command == "pt" || command == "pp" && config.shortCommands) {
            return PartyAPI.partyMembers
        }

        if (command == "p" || command == "party") {
            val friends = if (config.tabComplete.friends) {
                FriendAPI.getAllFriends().filter { it.bestFriend || !config.tabComplete.onlyBestFriends }.map { it.name }
            } else {
                emptyList<String>()
            }
            val allOnLobby = McWorld.players.map { it.name }
            return friends + getPartyCommands() + allOnLobby
        }
        return null
    }

    private fun getPartyCommands(): List<String> {
        return if (config.tabComplete.partyCommands && PartyAPI.partyMembers.isNotEmpty()) {
            otherPartyCommands
        } else emptyList()
    }

    @HandleEvent
    fun onConfigFix(event: ConfigFixEvent) {
        event.move(31, "commands", "misc.commands")
    }
}

private val otherPartyCommands = listOf(
    "Disband",
    "KickOffline",
    "Leave",
    "List",
    "Mute",
    "Private",
    "Warp",
    "Settings"
)
