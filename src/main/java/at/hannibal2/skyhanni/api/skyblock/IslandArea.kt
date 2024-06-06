package at.hannibal2.skyhanni.api.skyblock

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.utils.RepositoryReloadEvent

class IslandArea private constructor(private var name: String) {

    fun isInside(): Boolean {
        return SkyBlockAPI.area == name
    }

    companion object {

        private val areas = mutableMapOf<String, IslandArea>()

        val CRYSTAL_NUCLEUS = register("crystal_nucleus", "Crystal Nucleus")
        val DWARVEN_BASE_CAMP = register("dwarven_base_camp", "Dwarven Base Camp")
        val FOSSIL_RESEARCH_CENTER = register("fossil_research_center", "Fossil Research Center")
        val ARACHNE_SANCTUARY = register("arachnes_sanctuary", "Arachne's Sanctuary")
        val THE_MIST = register("the_mist", "The Mist")
        val COMMUNITY_CENTER = register("community_center", "Community Center")
        val DOJO = register("dojo", "Dojo")
        val BLAZING_VOLCANO = register("blazing_volcano", "Blazing Volcano")
        val GUNPOWDER_MINES = register("gunpowder_mines", "Gunpowder Mines")
        val COLOSSEUM = register("colosseum", "Colosseum")
        val INFESTED_HOUSE = register("infested_house", "Infested House")
        val WEST_VILLAGE = register("west_house", "West Village")
        val MIRRORVERSE = register("mirrorverse", "Mirrorverse")
        val DREADFARM = register("dreadfarm", "Dreadfarm")
        val LIVING_STILLNESS = register("living_stillness", "Living Stillness")
        val LIVING_CAVE = register("living_cave", "Living Cave")
        val STILLGORE_CHATEAU = register("stillgore_chateau", "Stillgore Château")
        val OUBLIETTE = register("oubliette", "Oubliette")
        val TRAPPERS_DEN = register("trappers_den", "Trapper's Den")
        val DRAGONTAIL = register("dragontail", "Dragontail")
        val BAZAAR_ALLEY = register("bazaar_alley", "Bazaar Alley")
        val ROYAL_PALACE = register("royal_palace", "Royal Palace")
        val DUNGEON_HUB = register("dungeon_hub", "Dungeon Hub")
        val FARM = register("farm", "Farm")

        @HandleEvent
        fun onRepoLoad(event: RepositoryReloadEvent) {
            for ((key, name) in event.getConstant<Map<String, String>>("IslandAreas")) {
                areas.getOrPut(key) { IslandArea(name) }.name = name
            }
        }

        private fun register(key: String, name: String): IslandArea {
            return areas.getOrPut(key) { IslandArea(name) }
        }
    }
}
