package at.hannibal2.skyhanni.features.misc.items

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.jsonobjects.repo.ItemsJson
import at.hannibal2.skyhanni.data.jsonobjects.repo.neu.NeuReforgeStoneJson
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.item.ItemHoverEvent
import at.hannibal2.skyhanni.events.render.gui.ChestGuiOverlayRenderEvent
import at.hannibal2.skyhanni.events.render.gui.GuiOverlayRenderEvent
import at.hannibal2.skyhanni.events.render.gui.RenderItemTooltipEvent
import at.hannibal2.skyhanni.events.utils.ConfigFixEvent
import at.hannibal2.skyhanni.events.utils.ConfigLoadEvent
import at.hannibal2.skyhanni.events.utils.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.utils.neu.NeuRepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addAsSingletonList
import at.hannibal2.skyhanni.utils.ConditionalUtils.onToggle
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.isRune
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getItemStackOrNull
import at.hannibal2.skyhanni.utils.NumberUtil
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RenderUtils.renderStringsAndItems
import at.hannibal2.skyhanni.utils.mc.McScreen
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import kotlin.math.roundToLong

@SkyHanniModule
object EstimatedItemValue {

    private val config get() = SkyHanniMod.feature.inventory.estimatedItemValues
    private var display = emptyList<List<Any>>()
    private val cache = mutableMapOf<ItemStack, List<List<Any>>>()
    private var lastToolTipTime = 0L
    var gemstoneUnlockCosts = HashMap<NEUInternalName, HashMap<String, List<String>>>()
    var reforges = mapOf<NEUInternalName, NeuReforgeStoneJson>()
    var bookBundleAmount = mapOf<String, Int>()
    private var currentlyShowing = false

    fun isCurrentlyShowing() = currentlyShowing && McScreen.isOpen

    @HandleEvent
    fun onNeuRepoReload(event: NeuRepositoryReloadEvent) {
        gemstoneUnlockCosts =
            event.readConstant<HashMap<NEUInternalName, HashMap<String, List<String>>>>("gemstonecosts")
        reforges =
            event.readConstant<Map<NEUInternalName, NeuReforgeStoneJson>>("reforgestones")
    }

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<ItemsJson>("Items")
        bookBundleAmount = data.bookBundleAmount
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onTooltip(event: ItemHoverEvent) {
        if (!config.enabled) return
        if (McScreen.screen !is GuiProfileViewer) return

        if (renderedItems == 0) {
            updateItem(event.itemStack)
        }
        tryRendering()
        renderedItems++
    }

    /**
     * Workaround for NEU Profile Viewer bug where the ItemTooltipEvent gets called for two items when hovering
     * over the border between two items.
     * Also fixes complications with ChatTriggers where they call the stack.getToolTips() method that causes the
     * ItemTooltipEvent to getting triggered multiple times per frame.
     */
    private var renderedItems = 0

    @HandleEvent
    fun onRenderOverlay(event: GuiOverlayRenderEvent) {
        renderedItems = 0
    }

    private fun tryRendering() {
        currentlyShowing = checkCurrentlyVisible()
        if (!currentlyShowing) return

        config.itemPriceDataPos.renderStringsAndItems(display, posLabel = "Estimated Item Value")
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onRenderOverlay(event: ChestGuiOverlayRenderEvent) {
        tryRendering()
    }

    private fun checkCurrentlyVisible(): Boolean {
        if (!config.enabled) return false
        if (!config.hotkey.isKeyHeld() && !config.alwaysEnabled) return false
        if (System.currentTimeMillis() > lastToolTipTime + 200) return false

        if (display.isEmpty()) return false

        return true
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        cache.clear()
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        config.enchantmentsCap.onToggle {
            cache.clear()
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onRenderItemTooltip(event: RenderItemTooltipEvent) {
        if (!config.enabled) return

        updateItem(event.stack)
    }

    private fun updateItem(item: ItemStack) {
        cache[item]?.let {
            display = it
            lastToolTipTime = System.currentTimeMillis()
            return
        }

        val openInventoryName = InventoryUtils.openInventoryName()
        if (openInventoryName.startsWith("Museum ")) {
            if (item.getLore().any { it.contains("Armor Set") }) {
                return
            }
        }
        if (openInventoryName == "Island Deliveries") {
            if (item.getLore().any { it == "§eClick to collect!" }) {
                return
            }
        }

        val newDisplay = try {
            draw(item)
        } catch (e: Exception) {
            ErrorManager.logErrorWithData(
                e, "Error in Estimated Item Value renderer",
                "openInventoryName" to openInventoryName,
                "item" to item,
                "item name" to item.itemName,
                "internal name" to item.getInternalNameOrNull(),
                "lore" to item.getLore(),
            )
            listOf()
        }

        cache[item] = newDisplay
        display = newDisplay
        lastToolTipTime = System.currentTimeMillis()
    }

    private fun draw(stack: ItemStack): List<List<Any>> {
        val internalName = stack.getInternalNameOrNull() ?: return listOf()

        // Stats Breakdown
        val name = stack.name
        if (name == "§6☘ Category: Item Ability (Passive)") return listOf()
        if (name.contains("Salesperson")) return listOf()

        // Autopet rule > Create Rule
        if (!InventoryUtils.isSlotInPlayerInventory(stack)) {
            if (InventoryUtils.openInventoryName() == "Choose a wardrobe slot") return listOf()
        }

        // FIX neu item list
        if (internalName.startsWith("ULTIMATE_ULTIMATE_")) return listOf()
        // We don't need this feature to work on books at all
        if (stack.item == Items.enchanted_book) return listOf()
        // Block catacombs items in mort inventory
        if (internalName.startsWith("CATACOMBS_PASS_") || internalName.startsWith("MASTER_CATACOMBS_PASS_")) return listOf()
        // Blocks the dungeon map
        if (internalName.startsWith("MAP-")) return listOf()
        // Hides the rune item
        if (internalName.isRune()) return listOf()
        if (internalName.contains("UNIQUE_RUNE")) return listOf()
        if (internalName.contains("WISP_POTION")) return listOf()


        if (internalName.getItemStackOrNull() == null) {
            ChatUtils.debug("Estimated Item Value is null for: '$internalName'")
            return listOf()
        }

        val list = mutableListOf<String>()
        list.add("§aEstimated Item Value:")
        val pair = EstimatedItemValueCalculator.calculate(stack, list)
        val (totalPrice, basePrice) = pair

        if (basePrice == totalPrice) return listOf()

        val numberFormat = if (config.exactPrice) {
            totalPrice.roundToLong().addSeparators()
        } else {
            NumberUtil.format(totalPrice)
        }
        list.add("§aTotal: §6§l$numberFormat coins")

        val newDisplay = mutableListOf<List<Any>>()
        for (line in list) {
            newDisplay.addAsSingletonList(line)
        }
        return newDisplay
    }

    @HandleEvent
    fun onConfigFix(event: ConfigFixEvent) {
        event.move(31, "misc.estimatedItemValues", "inventory.estimatedItemValues")
    }
}
