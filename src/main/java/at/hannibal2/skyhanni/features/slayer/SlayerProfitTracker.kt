package at.hannibal2.skyhanni.features.slayer

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.data.SlayerAPI
import at.hannibal2.skyhanni.data.jsonobjects.repo.SlayerProfitTrackerItemsJson
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.inventory.ItemAddEvent
import at.hannibal2.skyhanni.events.render.gui.GuiRenderEvent
import at.hannibal2.skyhanni.events.skyblock.PurseChangeCause
import at.hannibal2.skyhanni.events.skyblock.PurseChangeEvent
import at.hannibal2.skyhanni.events.slayer.SlayerChangeEvent
import at.hannibal2.skyhanni.events.slayer.SlayerQuestCompleteEvent
import at.hannibal2.skyhanni.events.utils.RepositoryReloadEvent
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addAsSingletonList
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NumberUtil
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatDouble
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.formatPercentage
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.tracker.ItemTrackerData
import at.hannibal2.skyhanni.utils.tracker.SkyHanniItemTracker
import com.google.gson.annotations.Expose

object SlayerProfitTracker {

    private val config get() = SkyHanniMod.feature.slayer.itemProfitTracker

    private var itemLogCategory = ""
    private var baseSlayerType = ""
    private val trackers = mutableMapOf<String, SkyHanniItemTracker<Data>>()

    /**
     * REGEX-TEST: §7Took 1.9k coins from your bank for auto-slayer...
     */
    private val autoSlayerBankPattern by RepoPattern.pattern(
        "slayer.autoslayer.bank.chat",
        "§7Took (?<coins>.+) coins from your bank for auto-slayer\\.\\.\\."
    )

    class Data : ItemTrackerData() {

        override fun resetItems() {
            slayerSpawnCost = 0
            slayerCompletedCount = 0
        }

        @Expose
        var slayerSpawnCost: Long = 0

        @Expose
        var slayerCompletedCount = 0L

        override fun getDescription(timesDropped: Long): List<String> {
            val percentage = timesDropped.toDouble() / slayerCompletedCount
            val perBoss = percentage.formatPercentage()

            return listOf(
                "§7Dropped §e${timesDropped.addSeparators()} §7times.",
                "§7Your drop rate: §c$perBoss",
            )
        }

        override fun getCoinName(item: TrackedItem) = "§6Mob Kill Coins"

        override fun getCoinDescription(item: TrackedItem): List<String> {
            val mobKillCoinsFormat = NumberUtil.format(item.totalAmount)
            return listOf(
                "§7Killing mobs gives you coins (more with scavenger).",
                "§7You got §6$mobKillCoinsFormat coins §7that way."
            )
        }
    }

    private fun addSlayerCosts(price: Double) {
        require(price < 0) { "slayer costs can not be positive" }
        getTracker()?.modify {
            it.slayerSpawnCost += price.toInt()
        }
    }

    private var allowedItems = mapOf<String, List<NEUInternalName>>()

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        allowedItems = event.getConstant<SlayerProfitTrackerItemsJson>("SlayerProfitTrackerItems").slayers
    }

    @HandleEvent
    fun onPurseChange(event: PurseChangeEvent) {
        if (!isEnabled()) return
        val coins = event.coins
        if (event.reason == PurseChangeCause.GAIN_MOB_KILL && SlayerAPI.isInCorrectArea) {
            getTracker()?.addCoins(coins.toInt())
        }
        if (event.reason == PurseChangeCause.LOSE_SLAYER_QUEST_STARTED) {
            addSlayerCosts(coins)
        }
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!isEnabled()) return
        autoSlayerBankPattern.matchMatcher(event.message) {
            addSlayerCosts(-group("coins").formatDouble())
        }
    }

    @HandleEvent
    fun onSlayerChange(event: SlayerChangeEvent) {
        val newSlayer = event.newSlayer
        itemLogCategory = newSlayer.removeColor()
        baseSlayerType = itemLogCategory.substringBeforeLast(" ")
        getTracker()?.update()
    }

    private fun getTracker(): SkyHanniItemTracker<Data>? {
        if (itemLogCategory == "") return null

        return trackers.getOrPut(itemLogCategory) {
            val getStorage: (ProfileSpecificStorage) -> Data = {
                it.slayerProfitData.getOrPut(
                    itemLogCategory
                ) { Data() }
            }
            SkyHanniItemTracker("$itemLogCategory Profit Tracker", { Data() }, getStorage) { drawDisplay(it) }
        }
    }

    @HandleEvent
    fun onQuestComplete(event: SlayerQuestCompleteEvent) {
        getTracker()?.modify {
            it.slayerCompletedCount++
        }
    }

    @HandleEvent
    fun onItemAdd(event: ItemAddEvent) {
        if (!isEnabled()) return
        if (!SlayerAPI.isInCorrectArea) return
        if (!SlayerAPI.hasActiveSlayerQuest()) return

        val internalName = event.internalName
        val amount = event.amount

        if (!isAllowedItem(internalName)) {
            ChatUtils.debug("Ignored non-slayer item pickup: '$internalName' '$itemLogCategory'")
            return
        }

        getTracker()?.addItem(internalName, amount)
    }

    private fun isAllowedItem(internalName: NEUInternalName): Boolean {
        val allowedList = allowedItems[baseSlayerType] ?: return false
        return internalName in allowedList
    }

    private fun drawDisplay(data: Data) = buildList<List<Any>> {
        val tracker = getTracker() ?: return@buildList
        addAsSingletonList("§e§l$itemLogCategory Profit Tracker")

        var profit = tracker.drawItems(data, { true }, this)
        val slayerSpawnCost = data.slayerSpawnCost
        if (slayerSpawnCost != 0L) {
            val mobKillCoinsFormat = NumberUtil.format(slayerSpawnCost)
            addAsSingletonList(
                Renderable.hoverTips(
                    " §7Slayer Spawn Costs: §c$mobKillCoinsFormat",
                    listOf("§7You paid §c$mobKillCoinsFormat §7in total", "§7for starting the slayer quests.")
                )
            )
            profit += slayerSpawnCost
        }

        val slayerCompletedCount = data.slayerCompletedCount
        addAsSingletonList(
            Renderable.hoverTips(
                "§7Bosses killed: §e${slayerCompletedCount.addSeparators()}",
                listOf("§7You killed the $itemLogCategory boss", "§e${slayerCompletedCount.addSeparators()} §7times.")
            )
        )

        addAsSingletonList(tracker.addTotalProfit(profit, data.slayerCompletedCount, "boss"))

        tracker.addPriceFromButton(this)
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return
        if (!SlayerAPI.isInCorrectArea) return

        getTracker()?.renderDisplay(config.pos)
    }

    fun isEnabled() = SkyBlockAPI.isConnected && config.enabled

    fun clearProfitCommand(args: Array<String>) {
        if (itemLogCategory == "") {
            ChatUtils.userError(
                "No current slayer data found! " +
                    "§eGo to a slayer area and start the specific slayer type you want to reset the data of.",
            )
            return
        }

        getTracker()?.resetCommand()
    }
}
