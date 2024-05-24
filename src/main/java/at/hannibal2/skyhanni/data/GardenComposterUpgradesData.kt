package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.model.ComposterUpgrade
import at.hannibal2.skyhanni.events.inventory.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.features.garden.composter.ComposterAPI
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimalIfNecessary
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher

object GardenComposterUpgradesData {

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (event.inventoryName != "Composter Upgrades") return
        for (item in event.inventoryItems.values) {
            ComposterUpgrade.regex.matchMatcher(item.name) {
                val name = group("name")
                val level = group("level")?.romanToDecimalIfNecessary() ?: 0
                val composterUpgrade = ComposterUpgrade.getByName(name)!!
                ComposterAPI.composterUpgrades?.put(composterUpgrade, level)
            }
        }
    }
}
