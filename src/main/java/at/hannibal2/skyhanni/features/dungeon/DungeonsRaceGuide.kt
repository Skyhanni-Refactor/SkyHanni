package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.data.jsonobjects.repo.DungeonHubRacesJson
import at.hannibal2.skyhanni.events.minecraft.ActionBarUpdateEvent
import at.hannibal2.skyhanni.events.render.world.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.skyblock.IslandChangeEvent
import at.hannibal2.skyhanni.events.utils.ConfigLoadEvent
import at.hannibal2.skyhanni.events.utils.RepositoryReloadEvent
import at.hannibal2.skyhanni.utils.ColourUtils.toChromaColour
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.ParkourHelper
import at.hannibal2.skyhanni.utils.RegexUtils.findMatcher
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

object DungeonsRaceGuide {

    private val config get() = SkyHanniMod.feature.dungeon.dungeonsRaceGuide
    private val raceActivePattern by RepoPattern.pattern(
        "dungeon.race.active",
        "§.§.(?<race>[\\w ]+) RACE §.[\\d:.]+"
    )

    private val parkourHelpers: MutableMap<String, ParkourHelper> = mutableMapOf()

    private var currentRace: String? = null

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        parkourHelpers.forEach { it.value.reset() }
        currentRace = null
    }

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<DungeonHubRacesJson>("DungeonHubRaces")
        data.data.forEach {
            val nothingNoReturn = it.value["nothing:no_return"]
            parkourHelpers[it.key] = ParkourHelper(
                nothingNoReturn?.locations ?: listOf(),
                nothingNoReturn?.shortCuts ?: listOf(),
                platformSize = 1.0,
                detectionRange = 7.0,
                depth = false,
            )
        }
        updateConfig()
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.rainbowColor, config.monochromeColor, config.lookAhead) {
            updateConfig()
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onActionBarUpdate(event: ActionBarUpdateEvent) {
        if (!isEnabled()) return
        currentRace = null
        raceActivePattern.findMatcher(event.actionBar) {
            currentRace = group("race").replace(" ", "_").lowercase()
        }
        if (currentRace == null) {
            parkourHelpers.forEach {
                it.value.reset()
            }
        }
    }

    private fun updateConfig() {
        parkourHelpers.forEach {
            it.value.rainbowColor = config.rainbowColor.get()
            it.value.monochromeColor = config.monochromeColor.get().toChromaColour()
            it.value.lookAhead = config.lookAhead.get() + 1
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return
        if (currentRace == null) return

        parkourHelpers[currentRace]?.render(event)
    }

    fun isEnabled() = IslandType.DUNGEON_HUB.isInIsland() && config.enabled
}
