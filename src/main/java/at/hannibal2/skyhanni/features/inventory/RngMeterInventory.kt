package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.render.gui.BackgroundDrawnEvent
import at.hannibal2.skyhanni.events.render.gui.RenderItemTipEvent
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

object RngMeterInventory {

    private val config get() = SkyHanniMod.feature.inventory.rngMeter

    private val meterPattern by RepoPattern.pattern(
        "inventory.rngmeter",
        "§8Catacombs (?<floor>\\w+)",
    )

    @HandleEvent
    fun onRenderItemTip(event: RenderItemTipEvent) {
        if (!config.floorName) return
        val chestName = InventoryUtils.openInventoryName()
        if (chestName != "Catacombs RNG Meter") return
        val stack = event.stack
        if (stack.name.removeColor() != "RNG Meter") return
        meterPattern.matchMatcher(stack.getLore()[0]) {
            event.stackTip = group("floor")
        }
    }

    @HandleEvent(priority = HandleEvent.LOW)
    fun onBackgroundDrawn(event: BackgroundDrawnEvent) {
        if (!SkyBlockAPI.isConnected) return

        val chestName = InventoryUtils.openInventoryName()
        if (config.noDrop && chestName == "Catacombs RNG Meter") {
            for (slot in InventoryUtils.getItemsInOpenChest()) {
                val stack = slot.stack
                if (stack.getLore().any { it.contains("You don't have an RNG drop") }) {
                    slot.highlight(LorenzColor.RED)
                }
            }
        }

        if (config.selectedDrop && chestName.endsWith(" RNG Meter")) {
            for (slot in InventoryUtils.getItemsInOpenChest()) {
                val stack = slot.stack
                if (stack.getLore().any { it.contains("§a§lSELECTED") }) {
                    slot.highlight(LorenzColor.YELLOW)
                }
            }
        }
    }
}
