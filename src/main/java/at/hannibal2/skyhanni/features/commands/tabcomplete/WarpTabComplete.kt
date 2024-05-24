package at.hannibal2.skyhanni.features.commands.tabcomplete

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.data.jsonobjects.repo.WarpsJson
import at.hannibal2.skyhanni.events.chat.TabCompletionEvent
import at.hannibal2.skyhanni.events.utils.RepositoryReloadEvent

object WarpTabComplete {

    private val config get() = SkyHanniMod.feature.misc.commands.tabComplete
    private var warps = listOf<String>()

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<WarpsJson>("Warps")
        warps = data.warpCommands
    }

    @HandleEvent
    fun onTabComplete(event: TabCompletionEvent) {
        if (event.isCommand("warp")) {
            event.addSuggestions(warps)
        }
    }

    fun isEnabled() = SkyBlockAPI.isConnected && config.warps
}
