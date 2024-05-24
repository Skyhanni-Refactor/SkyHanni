package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.data.MaxwellAPI
import at.hannibal2.skyhanni.events.render.gui.BackgroundDrawnEvent
import at.hannibal2.skyhanni.events.render.gui.RenderInventoryItemTipEvent
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.StringUtils.createCommaSeparatedList
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack

object StatsTuning {

    private val config get() = SkyHanniMod.feature.inventory.statsTuning

    private val statPointsPattern by RepoPattern.pattern(
        "inventory.statstuning.points",
        "§7Stat has: §e(?<amount>\\d+) points?"
    )

    @HandleEvent
    fun onRenderItemTip(event: RenderInventoryItemTipEvent) {
        val inventoryName = event.inventoryName

        val stack = event.stack

        if (config.templateStats && inventoryName == "Stats Tuning") if (templateStats(stack, event)) return
        if (config.selectedStats && MaxwellAPI.isThaumaturgyInventory(inventoryName) && renderTunings(
                stack,
                event
            )
        ) return
        if (config.points && inventoryName == "Stats Tuning") points(stack, event)
    }

    private fun templateStats(stack: ItemStack, event: RenderInventoryItemTipEvent): Boolean {
        if (stack.name != "§aLoad") return false

        var grab = false
        val list = mutableListOf<String>()
        for (line in stack.getLore()) {
            if (line == "§7You are loading:") {
                grab = true
                continue
            }
            if (!grab) continue

            if (line == "") {
                grab = false
                continue
            }
            val text = line.split(":")[0]
            list.add(text)
        }
        if (list.isEmpty()) return false

        event.stackTip = list.joinToString(" + ")
        event.offsetX = 20
        event.offsetY = -5
        event.alignLeft = false
        return true
    }

    private fun renderTunings(stack: ItemStack, event: RenderInventoryItemTipEvent): Boolean {
        if (stack.name != "§aStats Tuning") return false
        val tunings = MaxwellAPI.tunings ?: return false

        event.stackTip = tunings
            .map { tuning ->
                with(tuning) {
                    "$color$value$icon"
                }
            }
            .createCommaSeparatedList("§7")
        event.offsetX = 3
        event.offsetY = -5
        event.alignLeft = false
        return true
    }

    private fun points(stack: ItemStack, event: RenderInventoryItemTipEvent) {
        stack.getLore().matchFirst(statPointsPattern) {
            val points = group("amount")
            event.stackTip = points
        }
    }

    @HandleEvent(onlyOnSkyblock = true, priority = HandleEvent.LOW)
    fun onBackgroundDrawn(event: BackgroundDrawnEvent) {
        val chestName = InventoryUtils.openInventoryName()
        if (!config.selectedTemplate || chestName != "Stats Tuning") return
        for (slot in InventoryUtils.getItemsInOpenChest()) {
            val stack = slot.stack
            val lore = stack.getLore()

            if (lore.any { it == "§aCurrently selected!" }) {
                slot.highlight(LorenzColor.GREEN)
            }
        }
    }
}
