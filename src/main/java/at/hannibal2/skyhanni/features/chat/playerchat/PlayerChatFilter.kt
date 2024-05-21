package at.hannibal2.skyhanni.features.chat.playerchat

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.jsonobjects.repo.PlayerChatFilterJson
import at.hannibal2.skyhanni.events.utils.RepositoryReloadEvent
import at.hannibal2.skyhanni.utils.MultiFilter
import net.minecraft.util.IChatComponent

object PlayerChatFilter {

    private val filters = mutableMapOf<String, MultiFilter>()

    fun shouldChatFilter(original: IChatComponent): Boolean {
        val message = original.formattedText.lowercase()
        for (filter in filters) {
            filter.value.matchResult(message)?.let {
                return true
            }
        }
        return false
    }

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        filters.clear()
        var countFilters = 0

        val playerChatFilter = event.getConstant<PlayerChatFilterJson>("PlayerChatFilter")
        for (category in playerChatFilter.filters) {
            val description = category.description
            val filter = MultiFilter()
            filter.load(category)
            filters[description] = filter

            countFilters += filter.count()
        }
    }
}
