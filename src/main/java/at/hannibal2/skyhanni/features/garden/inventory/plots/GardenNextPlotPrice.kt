package at.hannibal2.skyhanni.features.garden.inventory.plots

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.events.item.SkyHanniToolTipEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getPrice
import at.hannibal2.skyhanni.utils.NumberUtil

@SkyHanniModule
object GardenNextPlotPrice {

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onTooltip(event: SkyHanniToolTipEvent) {
        if (!GardenAPI.config.plotPrice) return

        if (InventoryUtils.openInventoryName() != "Configure Plots") return

        if (!event.itemStack.name.startsWith("§ePlot")) return

        var next = false
        val list = event.toolTip
        var i = -1
        for (line in event.toolTipRemovedPrefix()) {
            i++
            if (line.contains("Cost")) {
                next = true
                continue
            }

            if (next) {
                val readItemAmount = ItemUtils.readItemAmount(line)
                readItemAmount?.let {
                    val (itemName, amount) = it
                    val lowestBin = NEUInternalName.fromItemName(itemName).getPrice()
                    val price = lowestBin * amount
                    val format = NumberUtil.format(price)
                    list[i] = list[i] + " §7(§6$format§7)"
                } ?: run {
                    ErrorManager.logErrorStateWithData(
                        "Garden Next Plot Price error",
                        "Could not read item amount from line",
                        "line" to line,
                        "event.toolTip" to event.toolTip,
                    )
                }
                break
            }
        }
    }
}
