package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.inventory.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.features.garden.visitor.VisitorAPI
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.DisplayTableEntry
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getPrice
import at.hannibal2.skyhanni.utils.NumberUtil
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AnitaMedalProfit {

    private val config get() = GardenAPI.config.anitaShop
    private var display = emptyList<Renderable>()

    var inInventory = false

    enum class MedalType(val displayName: String, val factorBronze: Int) {
        GOLD("§6Gold medal", 8),
        SILVER("§fSilver medal", 2),
        BRONZE("§cBronze medal", 1),
        ;
    }

    private fun getMedal(name: String) = MedalType.entries.firstOrNull { it.displayName == name }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
    }

    @HandleEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!config.medalProfitEnabled) return
        if (event.inventoryName != "Anita") return
        if (VisitorAPI.inInventory) return

        inInventory = true

        val table = mutableListOf<DisplayTableEntry>()
        for ((slot, item) in event.inventoryItems) {
            try {
                readItem(slot, item, table)
            } catch (e: Throwable) {
                ErrorManager.logErrorWithData(
                    e, "Error in AnitaMedalProfit while reading item '${item.itemName}'",
                    "item" to item,
                    "name" to item.itemName,
                    "inventory name" to InventoryUtils.openInventoryName(),
                )
            }
        }

        val newList = mutableListOf<Renderable>()
        newList.add(Renderable.string("§eMedal Profit"))
        newList.add(LorenzUtils.fillTable(table, padding = 5, itemScale = 0.7))
        display = newList
    }

    private fun readItem(slot: Int, item: ItemStack, table: MutableList<DisplayTableEntry>) {
        val itemName = getItemName(item)
        if (itemName == " ") return
        if (itemName == "§cClose") return
        if (itemName == "§eUnique Gold Medals") return
        if (itemName == "§aMedal Trades") return

        val fullCost = getFullCost(getRequiredItems(item))
        if (fullCost < 0) return

        val (name, amount) = ItemUtils.readItemAmount(itemName) ?: return

        var internalName = NEUInternalName.fromItemNameOrNull(name)
        if (internalName == null) {
            internalName = item.getInternalName()
        }

        val itemPrice = internalName.getPrice() * amount
        if (itemPrice < 0) return

        val profit = itemPrice - fullCost
        val profitFormat = NumberUtil.format(profit)
        val color = if (profit > 0) "§6" else "§c"

        val hover = listOf(
            itemName,
            "",
            "§7Item price: §6${NumberUtil.format(itemPrice)} ",
            // TODO add more exact material cost breakdown
            "§7Material cost: §6${NumberUtil.format(fullCost)} ",
            "§7Final profit: §6${profitFormat} ",
        )
        table.add(
            DisplayTableEntry(
                itemName,
                "$color$profitFormat",
                profit,
                internalName,
                hover,
                highlightsOnHoverSlots = listOf(slot)
            )
        )
    }

    private fun getItemName(item: ItemStack): String {
        val name = item.name
        val isEnchantedBook = item.getItemCategoryOrNull() == ItemCategory.ENCHANTED_BOOK
        return if (isEnchantedBook) {
            item.itemName
        } else name
    }

    private fun getFullCost(requiredItems: MutableList<String>): Double {
        val jacobTicketPrice = SkyhanniItems.JACOBS_TICKET().getPrice()
        var otherItemsPrice = 0.0
        for (rawItemName in requiredItems) {
            val pair = ItemUtils.readItemAmount(rawItemName)
            if (pair == null) {
                ErrorManager.logErrorStateWithData(
                    "Error in Anita Medal Contest", "Could not read item amount",
                    "rawItemName" to rawItemName,
                )
                continue
            }

            val (name, amount) = pair
            val medal = getMedal(name)
            otherItemsPrice += if (medal != null) {
                val bronze = medal.factorBronze * amount
                bronze * jacobTicketPrice
            } else {
                NEUInternalName.fromItemName(name).getPrice() * amount
            }
        }
        return otherItemsPrice
    }

    private fun getRequiredItems(item: ItemStack): MutableList<String> {
        val items = mutableListOf<String>()
        var next = false
        for (line in item.getLore()) {
            if (line == "§7Cost") {
                next = true
                continue
            }
            if (next) {
                if (line == "") {
                    next = false
                    continue
                }

                items.add(line.replace("§8 ", " §8"))
            }
        }
        return items
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (inInventory) {
            config.medalProfitPos.renderRenderables(
                display,
                extraSpace = 5,
                posLabel = "Anita Medal Profit"
            )
        }
    }
}
