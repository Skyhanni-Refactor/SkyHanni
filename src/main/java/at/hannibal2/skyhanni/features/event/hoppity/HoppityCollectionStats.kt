package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.utils.DisplayTableEntry
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object HoppityCollectionStats {

    private val config get() = ChocolateFactoryAPI.config

    private val patternGroup = ChocolateFactoryAPI.patternGroup.group("collection")
    private val pagePattern by patternGroup.pattern(
        "page.current",
        "\\((?<page>\\d+)/(?<maxPage>\\d+)\\) Hoppity's Collection"
    )
    private val rabbitRarityPattern by patternGroup.pattern(
        "rabbit.rarity",
        "§.§L(?<rarity>\\w+) RABBIT"
    )
    private val duplicatesFoundPattern by patternGroup.pattern(
        "duplicates.found",
        "§7Duplicates Found: §a(?<duplicates>[\\d,]+)"
    )
    private val rabbitNotFoundPattern by patternGroup.pattern(
        "rabbit.notfound",
        "(?:§.)+You have not found this rabbit yet!"
    )
    private val rabbitsFoundPattern by patternGroup.pattern(
        "rabbits.found",
        "§.§l§m[ §a-z]+§r §.(?<current>[0-9]+)§./§.(?<total>[0-9]+)"
    )

    private var display = emptyList<Renderable>()
    private val loggedRabbits = mutableMapOf<String, RabbitCollectionInfo>()
    private var totalRabbits = 0
    var inInventory = false
    private var currentPage = 0

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return
        if (!pagePattern.matches(event.inventoryName)) return

        inInventory = true
        display = buildDisplay(event)
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
    }

    @SubscribeEvent
    fun onProfileChange(event: ProfileJoinEvent) {
        display = emptyList()
        loggedRabbits.clear()
        currentPage = 0
        inInventory = false
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!inInventory) return

        config.hoppityStatsPosition.renderRenderables(
            display,
            extraSpace = 5,
            posLabel = "Hoppity's Collection Stats"
        )
    }

    private fun buildDisplay(event: InventoryFullyOpenedEvent): MutableList<Renderable> {
        val totalAmount = logRabbits(event)

        val newList = mutableListOf<Renderable>()
        newList.add(Renderable.string("§eHoppity Rabbit Collection§f:"))
        newList.add(LorenzUtils.fillTable(getRabbitStats(), padding = 5))

        if (totalAmount != totalRabbits) {
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
        var totalAmountFound = 0
        var totalDuplicates = 0
        var totalChocolatePerSecond = 0
        var totalChocolateMultiplier = 0.0
        totalRabbits = 0

        val table = mutableListOf<DisplayTableEntry>()
        for (rarity in RabbitCollectionRarity.entries) {
            val filtered = loggedRabbits.filter { it.value.rarity == rarity }

            val isTotal = rarity == RabbitCollectionRarity.TOTAL

            val title = "${rarity.displayName} Rabbits"
            val amountFound = filtered.filter { it.value.found }.size
            val totalOfRarity = filtered.size
            val duplicates = filtered.values.sumOf { it.duplicates }
            val chocolatePerSecond = rarity.chocolatePerSecond * amountFound
            val chocolateMultiplier = (rarity.chocolateMultiplier * amountFound)

            if (!isTotal) {
                totalAmountFound += amountFound
                totalRabbits += totalOfRarity
                totalDuplicates += duplicates
                totalChocolatePerSecond += chocolatePerSecond
                totalChocolateMultiplier += chocolateMultiplier
            }

            val displayFound = if (isTotal) totalAmountFound else amountFound
            val displayTotal = if (isTotal) totalRabbits else totalOfRarity
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
                add("§7Chocolate Per Second: §a$displayChocolatePerSecond")
                add("§7Chocolate Multiplier: §a${displayChocolateMultiplier.roundTo(3)}")
            }
            table.add(
                DisplayTableEntry(
                    title,
                    "§a$displayFound§7/§a$displayTotal",
                    displayFound.toDouble(),
                    rarity.item,
                    hover
                )
            )
        }
        return table
    }

    private fun logRabbits(event: InventoryFullyOpenedEvent): Int {
        var totalAmount = 0

        for ((_, item) in event.inventoryItems) {
            val itemName = item.displayName ?: continue
            val itemLore = item.getLore()

            var duplicatesFound = 0
            var rabbitRarity: RabbitCollectionRarity? = null
            var found = true

            for (line in itemLore) {
                rabbitRarityPattern.matchMatcher(line) {
                    rabbitRarity = RabbitCollectionRarity.fromDisplayName(group("rarity"))
                }
                duplicatesFoundPattern.matchMatcher(line) {
                    duplicatesFound = group("duplicates").formatInt()
                }
                if (rabbitNotFoundPattern.matches(line)) found = false

                rabbitsFoundPattern.matchMatcher(line) {
                    totalAmount = group("total").formatInt()
                }
            }

            val rarity = rabbitRarity ?: continue

            if (itemName == "§dEinstein" && found) {
                ChocolateFactoryAPI.profileStorage?.timeTowerCooldown = 7
            }

            val duplicates = duplicatesFound.coerceAtLeast(0)
            loggedRabbits[itemName] = RabbitCollectionInfo(rarity, found, duplicates)
        }
        return totalAmount
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.hoppityCollectionStats

    private data class RabbitCollectionInfo(
        val rarity: RabbitCollectionRarity,
        val found: Boolean,
        val duplicates: Int,
    )

    // todo in future make the amount and multiplier work with mythic rabbits (can't until I have some)
    private enum class RabbitCollectionRarity(
        val displayName: String,
        val chocolatePerSecond: Int,
        val chocolateMultiplier: Double,
        val item: NEUInternalName,
    ) {
        COMMON("§fCommon", 1, 0.002, SkyhanniItems.WHITE_STAINED_GLASS()),
        UNCOMMON("§aUncommon", 2, 0.003, SkyhanniItems.LIME_STAINED_GLASS()),
        RARE("§9Rare", 4, 0.004, SkyhanniItems.BLUE_STAINED_GLASS()),
        EPIC("§5Epic", 10, 0.005, SkyhanniItems.PURPLE_STAINED_GLASS()),
        LEGENDARY("§6Legendary", 0, 0.02, SkyhanniItems.ORANGE_STAINED_GLASS()),
        MYTHIC("§dMythic", 0, 0.0, SkyhanniItems.PINK_STAINED_GLASS()),
        TOTAL("§cTotal", 0, 0.0, SkyhanniItems.RED_STAINED_GLASS()),
        ;

        companion object {
            fun fromDisplayName(displayName: String) = entries.firstOrNull { it.name == displayName }
        }
    }
}
