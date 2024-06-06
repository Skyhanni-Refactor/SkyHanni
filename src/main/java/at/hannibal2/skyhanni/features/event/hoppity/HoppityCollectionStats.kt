package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.inventory.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.render.gui.BackgroundDrawnEvent
import at.hannibal2.skyhanni.events.render.gui.ChestGuiOverlayRenderEvent
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DisplayTableEntry
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RegexUtils.anyMatches
import at.hannibal2.skyhanni.utils.RegexUtils.find
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.renderables.Renderable

@SkyHanniModule
object HoppityCollectionStats {

    private val config get() = ChocolateFactoryAPI.config

    private val patternGroup = ChocolateFactoryAPI.patternGroup.group("collection")
    private val pagePattern by patternGroup.pattern(
        "page.current",
        "\\((?<page>\\d+)/(?<maxPage>\\d+)\\) Hoppity's Collection"
    )
    private val duplicatesFoundPattern by patternGroup.pattern(
        "duplicates.found",
        "§7Duplicates Found: §a(?<duplicates>[\\d,]+)"
    )

    /**
     * REGEX-TEST: §7§8You cannot find this rabbit until you
     * REGEX-TEST: §7§8You have not found this rabbit yet!
     */
    private val rabbitNotFoundPattern by patternGroup.pattern(
        "rabbit.notfound",
        "(?:§.)+You (?:have not found this rabbit yet!|cannot find this rabbit until you)"
    )

    private val rabbitsFoundPattern by patternGroup.pattern(
        "rabbits.found",
        "§.§l§m[ §a-z]+§r §.(?<current>[0-9]+)§./§.(?<total>[0-9]+)"
    )
    /**
     * REGEX-TEST: §a✔ §7Requirement
     */
    private val requirementMet by patternGroup.pattern(
        "rabbit.requirement.met",
        "§a✔ §7Requirement"
    )
    /**
     * REGEX-TEST: §c✖ §7Requirement §e0§7/§a15
     * REGEX-TEST: §c✖ §7Requirement §e6§7/§a20
     * REGEX-TEST: §c✖ §7Requirement §e651§7/§a1,000
     */
    private val requirementNotMet by patternGroup.pattern(
        "rabbit.requirement.notmet",
        "§c✖ §7Requirement.*",
    )

    private var display = emptyList<Renderable>()
    private val loggedRabbits
        get() = ProfileStorageData.profileSpecific?.chocolateFactory?.rabbitCounts ?: mutableMapOf()

    var inInventory = false

    @HandleEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return
        if (!pagePattern.matches(event.inventoryName)) return

        inInventory = true
        display = buildDisplay(event)
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
        display = emptyList()
    }

    @HandleEvent
    fun onRenderOverlay(event: ChestGuiOverlayRenderEvent) {
        if (!inInventory) return

        config.hoppityStatsPosition.renderRenderables(
            display,
            extraSpace = 5,
            posLabel = "Hoppity's Collection Stats"
        )
    }

    // TODO cache with inventory update event
    @HandleEvent
    fun onBackgroundDrawn(event: BackgroundDrawnEvent) {
        if (!config.highlightRabbitsWithRequirement) return
        if (!inInventory) return

        for (slot in InventoryUtils.getItemsInOpenChest()) {
            val lore = slot.stack.getLore()
            if (lore.any { requirementMet.find(it) } && !config.onlyHighlightRequirementNotMet)
                slot.highlight(LorenzColor.GREEN)
            if (lore.any { requirementNotMet.find(it) })
                slot.highlight(LorenzColor.RED)
        }
    }

    private fun buildDisplay(event: InventoryFullyOpenedEvent): MutableList<Renderable> {
        logRabbits(event)

        val newList = mutableListOf<Renderable>()
        newList.add(Renderable.string("§eHoppity Rabbit Collection§f:"))
        newList.add(LorenzUtils.fillTable(getRabbitStats(), padding = 5))

        val loggedRabbitCount = loggedRabbits.size
        val foundRabbitCount = getFoundRabbitsFromHypixel(event)

        if (loggedRabbitCount < foundRabbitCount) {
            newList.add(Renderable.string(""))
            newList.add(
                Renderable.wrappedString(
                    "§cPlease Scroll through \n" +
                        "§call pages!",
                    width = 200,
                )
            )
        }
        return newList
    }

    private fun getRabbitStats(): MutableList<DisplayTableEntry> {
        var totalUniquesFound = 0
        var totalDuplicates = 0
        var totalChocolatePerSecond = 0
        var totalChocolateMultiplier = 0.0

        val table = mutableListOf<DisplayTableEntry>()
        for (rarity in RabbitCollectionRarity.entries) {
            val isTotal = rarity == RabbitCollectionRarity.TOTAL

            val foundOfRarity = loggedRabbits.filterKeys {
                HoppityCollectionData.getRarity(it) == rarity
            }

            val title = "${rarity.displayName} Rabbits"
            val uniquesFound = foundOfRarity.size
            val duplicates = foundOfRarity.values.sum() - uniquesFound

            val chocolateBonuses = foundOfRarity.keys.map {
                HoppityCollectionData.getChocolateBonuses(it)
            }

            val chocolatePerSecond = chocolateBonuses.sumOf { it.chocolate }
            val chocolateMultiplier = chocolateBonuses.sumOf { it.multiplier }

            if (hasFoundRabbit("Sigma") && rarity == RabbitCollectionRarity.MYTHIC) {
                totalChocolatePerSecond += uniquesFound * 5
            }

            if (!isTotal) {
                totalUniquesFound += uniquesFound
                totalDuplicates += duplicates
                totalChocolatePerSecond += chocolatePerSecond
                totalChocolateMultiplier += chocolateMultiplier
            }

            val displayFound = if (isTotal) totalUniquesFound else uniquesFound
            val displayTotal = if (isTotal) {
                HoppityCollectionData.knownRabbitCount
            } else {
                HoppityCollectionData.knownRabbitsOfRarity(rarity)
            }
            val displayDuplicates = if (isTotal) totalDuplicates else duplicates
            val displayChocolatePerSecond = if (isTotal) totalChocolatePerSecond else chocolatePerSecond
            val displayChocolateMultiplier = if (isTotal) totalChocolateMultiplier else chocolateMultiplier

            val hover = buildList {
                add(title)
                add("")
                add("§7Unique Rabbits: §a$displayFound§7/§a$displayTotal")
                add("§7Duplicate Rabbits: §a$displayDuplicates")
                add("§7Total Rabbits Found: §a${displayFound + displayDuplicates}")
                add("")
                add("§7Chocolate Per Second: §a${displayChocolatePerSecond.addSeparators()}")
                add("§7Chocolate Multiplier: §a${displayChocolateMultiplier.roundTo(3)}")
            }
            table.add(
                DisplayTableEntry(
                    title,
                    "§a$displayFound§7/§a$displayTotal",
                    displayTotal.toDouble(),
                    rarity.item,
                    hover
                )
            )
        }
        return table
    }

    fun incrementRabbit(name: String) {
        val rabbit = name.removeColor()
        if (!HoppityCollectionData.isKnownRabbit(rabbit)) return
        loggedRabbits[rabbit] = (loggedRabbits[rabbit] ?: 0) + 1
    }

    // Gets the found rabbits according to the Hypixel progress bar
    // used to make sure that mod data is synchronized with Hypixel
    private fun getFoundRabbitsFromHypixel(event: InventoryFullyOpenedEvent): Int {
        return event.inventoryItems.firstNotNullOf {
            it.value.getLore().matchFirst(rabbitsFoundPattern) {
                group("current").formatInt()
            }
        }
    }

    private fun logRabbits(event: InventoryFullyOpenedEvent) {
        for ((_, item) in event.inventoryItems) {
            val itemName = item.displayName?.removeColor() ?: continue
            val isRabbit = HoppityCollectionData.isKnownRabbit(itemName)

            if (!isRabbit) continue

            val itemLore = item.getLore()
            val found = !rabbitNotFoundPattern.anyMatches(itemLore)

            if (!found) continue

            val duplicates = itemLore.matchFirst(duplicatesFoundPattern) {
                group("duplicates").formatInt()
            } ?: 0

            loggedRabbits[itemName] = duplicates + 1
        }
    }


    // bugfix for some weird potential user errors (e.g. if users play on alpha and get rabbits)
    fun clearSavedRabbits() {
        loggedRabbits.clear()
        ChatUtils.chat("Cleared saved rabbit data.")
    }

    fun hasFoundRabbit(rabbit: String): Boolean = loggedRabbits.containsKey(rabbit)

    private fun isEnabled() = SkyBlockAPI.isConnected && config.hoppityCollectionStats

    enum class RabbitCollectionRarity(
        val displayName: String,
        val item: NEUInternalName,
    ) {
        COMMON("§fCommon", SkyhanniItems.WHITE_STAINED_GLASS()),
        UNCOMMON("§aUncommon", SkyhanniItems.LIME_STAINED_GLASS()),
        RARE("§9Rare", SkyhanniItems.BLUE_STAINED_GLASS()),
        EPIC("§5Epic", SkyhanniItems.PURPLE_STAINED_GLASS()),
        LEGENDARY("§6Legendary", SkyhanniItems.ORANGE_STAINED_GLASS()),
        MYTHIC("§dMythic", SkyhanniItems.PINK_STAINED_GLASS()),
        DIVINE("§bDivine", SkyhanniItems.AQUA_STAINED_GLASS()),
        TOTAL("§cTotal", SkyhanniItems.RED_STAINED_GLASS()),
        ;

        companion object {
            fun fromDisplayName(displayName: String) = entries.firstOrNull { it.name == displayName }
        }
    }
}
