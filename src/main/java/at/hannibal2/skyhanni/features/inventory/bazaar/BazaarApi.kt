package at.hannibal2.skyhanni.features.inventory.bazaar

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.data.bazaar.HypixelBazaarFetcher
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.inventory.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.render.gui.BackgroundDrawnEvent
import at.hannibal2.skyhanni.events.skyblock.BazaarOpenedProductEvent
import at.hannibal2.skyhanni.events.utils.ConfigFixEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.features.nether.kuudra.KuudraAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.InventoryUtils.getAllItems
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.StringUtils.equalsIgnoreColor
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.system.OS
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack

@SkyHanniModule
object BazaarApi {
    var inBazaarInventory = false
    private var currentSearchedItem = ""

    var currentlyOpenedProduct: NEUInternalName? = null

    fun NEUInternalName.getBazaarData(): BazaarData? = HypixelBazaarFetcher.latestProductInformation[this]

    fun NEUInternalName.getBazaarDataOrError(): BazaarData = getBazaarData() ?: run {
        ErrorManager.skyHanniError(
            "Can not find bazaar data for $itemName",
            "internal name" to this
        )
    }

    fun isBazaarItem(stack: ItemStack): Boolean = stack.getInternalName().isBazaarItem()

    fun NEUInternalName.isBazaarItem() = getBazaarData() != null

    fun searchForBazaarItem(displayName: String, amount: Int = -1) {
        if (!SkyBlockAPI.isConnected) return
        if (NEUItems.neuHasFocus()) return
        if (SkyBlockAPI.gamemode.noTrade) return
        if (DungeonAPI.inDungeon() || KuudraAPI.inKuudra) return
        HypixelCommands.bazaar(displayName.removeColor())
        if (amount != -1) OS.copyToClipboard(amount.toString())
        currentSearchedItem = displayName.removeColor()
    }

    @HandleEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        inBazaarInventory = checkIfInBazaar(event)
        if (inBazaarInventory) {
            val openedProduct = getOpenedProduct(event.inventoryItems) ?: return
            currentlyOpenedProduct = openedProduct
            BazaarOpenedProductEvent(openedProduct).post()
        }
    }

    private fun getOpenedProduct(inventoryItems: Map<Int, ItemStack>): NEUInternalName? {
        val buyInstantly = inventoryItems[10] ?: return null

        if (buyInstantly.displayName != "§aBuy Instantly") return null
        val bazaarItem = inventoryItems[13] ?: return null

        return NEUInternalName.fromItemName(bazaarItem.displayName)
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onBackgroundDrawn(event: BackgroundDrawnEvent) {
        if (!inBazaarInventory) return
        if (!SkyHanniMod.feature.inventory.bazaar.purchaseHelper) return
        if (currentSearchedItem == "") return

        if (event.gui !is GuiChest) return
        val guiChest = event.gui
        val chest = guiChest.inventorySlots as ContainerChest

        for ((slot, stack) in chest.getAllItems()) {
            if (chest.inventorySlots.indexOf(slot) !in 9..44) {
                continue
            }

            if (stack.displayName.removeColor() == currentSearchedItem) {
                slot.highlight(LorenzColor.GREEN)
            }
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onChat(event: SkyHanniChatEvent) {
        if (!inBazaarInventory) return
        // TODO USE SH-REPO
        // TODO remove dynamic pattern
        "\\[Bazaar] (Buy Order Setup!|Bought).*$currentSearchedItem.*".toPattern()
            .matchMatcher(event.message.removeColor()) { currentSearchedItem = "" }
    }

    private fun checkIfInBazaar(event: InventoryFullyOpenedEvent): Boolean {
        val items = event.inventorySize.let { listOf(it - 5, it - 6) }.mapNotNull { event.inventoryItems[it] }
        if (items.any { it.name.equalsIgnoreColor("Go Back") && it.getLore().firstOrNull() == "§7To Bazaar" }) {
            return true
        }

        if (event.inventoryName.startsWith("Bazaar ➜ ")) return true
        return when (event.inventoryName) {
            "How many do you want?" -> true
            "How much do you want to pay?" -> true
            "Confirm Buy Order" -> true
            "Confirm Instant Buy" -> true
            "At what price are you selling?" -> true
            "Confirm Sell Offer" -> true
            "Order options" -> true

            else -> false
        }
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inBazaarInventory = false
        currentlyOpenedProduct = null
    }

    @HandleEvent
    fun onConfigFix(event: ConfigFixEvent) {
        event.move(25, "bazaar", "inventory.bazaar")
    }
}
