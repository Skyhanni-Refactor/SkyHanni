package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceSqToPlayer
import at.hannibal2.skyhanni.utils.LorenzVec

object HoppityUniqueEggLocations {

    private val collectedEggLocations: MutableMap<IslandType, MutableSet<LorenzVec>>?
        get() = ChocolateFactoryAPI.profileStorage?.collectedEggLocations

    private fun getCurrentIslandCollectedEggs(): MutableSet<LorenzVec>? =
        collectedEggLocations?.getOrPut(SkyBlockAPI.island) { mutableSetOf() }

    fun saveNearestEgg() {
        val location = HoppityEggLocator.getCurrentIslandEggLocations()
            ?.minByOrNull { it.distanceSqToPlayer() } ?: return
        if (location.distanceSqToPlayer() > 100) {
            ErrorManager.skyHanniError(
                "Player far from any known egg location!",
                "island" to SkyBlockAPI.island,
                "distanceSqToPlayer" to location.distanceSqToPlayer(),
                "playerLocation" to LocationUtils.playerLocation(),
                "closestKnownEgg" to location,
            )
        }

        getCurrentIslandCollectedEggs()?.add(location)
    }

    fun collectedEggsThisIsland() = getCurrentIslandCollectedEggs()?.size ?: 0

    fun hasCollectedEgg(location: LorenzVec) =
        getCurrentIslandCollectedEggs()?.contains(location) ?: false

}
