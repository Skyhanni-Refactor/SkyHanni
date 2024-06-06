package at.hannibal2.skyhanni.features.garden.inventory

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.data.GardenCropMilestones
import at.hannibal2.skyhanni.data.GardenCropMilestones.getCounter
import at.hannibal2.skyhanni.data.model.ComposterUpgrade
import at.hannibal2.skyhanni.events.render.gui.RenderItemTipEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimalIfNecessary
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
object GardenInventoryNumbers {

    private val config get() = GardenAPI.config.number

    private val upgradeTierPattern by RepoPattern.pattern(
        "garden.inventory.numbers.upgradetier",
        "§7Current Tier: §[ea](?<tier>.*)§7/§a.*"
    )

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onRenderItemTip(event: RenderItemTipEvent) {
        if (InventoryUtils.openInventoryName() == "Crop Milestones") {
            if (!config.cropMilestone) return

            val crop = GardenCropMilestones.getCropTypeByLore(event.stack) ?: return
            val counter = crop.getCounter()
            val allowOverflow = GardenAPI.config.cropMilestones.overflow.inventoryStackSize
            val currentTier = GardenCropMilestones.getTierForCropCount(counter, crop, allowOverflow)
            event.stackTip = "" + currentTier
        }

        if (InventoryUtils.openInventoryName() == "Crop Upgrades") {
            if (!config.cropUpgrades) return

            event.stack.getLore().matchFirst(upgradeTierPattern) {
                event.stackTip = group("tier")
            }
        }

        if (InventoryUtils.openInventoryName() == "Composter Upgrades") {
            if (!config.composterUpgrades) return

            ComposterUpgrade.regex.matchMatcher(event.stack.name) {
                val level = group("level")?.romanToDecimalIfNecessary() ?: 0
                event.stackTip = "$level"
            }
        }
    }
}
