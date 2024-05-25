package at.hannibal2.skyhanni.features.rift.area.westvillage

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandArea
import at.hannibal2.skyhanni.api.skyblock.IslandType
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.inventory.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.render.gui.GuiRenderEvent
import at.hannibal2.skyhanni.events.skyblock.IslandChangeEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.utils.CollectionUtils.addAsSingletonList
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.mc.McPlayer
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker
import at.hannibal2.skyhanni.utils.tracker.TrackerData
import com.google.gson.annotations.Expose
import java.util.regex.Pattern

object VerminTracker {

    private val patternGroup = RepoPattern.group("rift.area.westvillage.vermintracker")
    private val silverfishPattern by patternGroup.pattern(
        "silverfish",
        ".*§eYou vacuumed a §.*Silverfish.*"
    )
    private val spiderPattern by patternGroup.pattern(
        "spider",
        ".*§eYou vacuumed a §.*Spider.*"
    )
    private val flyPattern by patternGroup.pattern(
        "fly",
        ".*§eYou vacuumed a §.*Fly.*"
    )
    private val verminPattern by patternGroup.pattern(
        "vermin",
        "§f(?:Vermin Bin|Vacuum Bag): §\\w(?<count>\\d+) (?<vermin>\\w+)"
    )

    private var hasVacuum = false

    private val config get() = RiftAPI.config.area.westVillage.verminTracker

    private val tracker = SkyHanniTracker("Vermin Tracker", { Data() }, { it.rift.verminTracker })
    { drawDisplay(it) }

    class Data : TrackerData() {

        override fun reset() {
            count.clear()
        }

        @Expose
        var count: MutableMap<VerminType, Int> = mutableMapOf()
    }

    enum class VerminType(val order: Int, val vermin: String, val pattern: Pattern) {
        FLY(1, "§aFlies", flyPattern),
        SPIDER(2, "§aSpiders", spiderPattern),
        SILVERFISH(3, "§aSilverfish", silverfishPattern),
    }

    @HandleEvent(onlyOnIsland = IslandType.THE_RIFT)
    fun onSecondPassed(event: SecondPassedEvent) {
        hasVacuum = McPlayer.has(SkyhanniItems.TURBOMAX_VACUUM(), true)
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        VerminType.entries.forEach { verminType ->
            if (verminType.pattern.matches(event.message)) {
                tracker.modify { it.count.addOrPut(verminType, 1) }

                if (config.hideChat) {
                    event.blockedReason = "vermin_vacuumed"
                }
            }
        }
    }

    @HandleEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!RiftAPI.inRift() || event.inventoryName != "Vermin Bin") return

        val bin = event.inventoryItems[13]?.getLore() ?: return
        val bag = McPlayer.inventory
            .firstOrNull { it.getInternalName() == SkyhanniItems.TURBOMAX_VACUUM() }
            ?.getLore() ?: emptyList()

        val binCounts = countVermin(bin)
        VerminType.entries.forEach { setVermin(it, binCounts[it] ?: 0) }

        if (bag.isEmpty()) return

        val bagCounts = countVermin(bag)
        VerminType.entries.forEach { addVermin(it, bagCounts[it] ?: 0) }
    }

    private fun countVermin(lore: List<String>): Map<VerminType, Int> {
        val verminCounts = mutableMapOf(
            VerminType.SILVERFISH to 0,
            VerminType.SPIDER to 0,
            VerminType.FLY to 0
        )
        for (line in lore) {
            verminPattern.matchMatcher(line) {
                val vermin = group("vermin").lowercase()
                val verminCount = group("count").toInt()
                val verminType = getVerminType(vermin)
                verminCounts[verminType] = verminCount
            }
        }
        return verminCounts
    }

    private fun getVerminType(vermin: String): VerminType {
        return when (vermin) {
            "silverfish", "silverfishes" -> VerminType.SILVERFISH
            "spider", "spiders" -> VerminType.SPIDER
            "fly", "flies" -> VerminType.FLY
            else -> VerminType.SILVERFISH
        }
    }

    private fun addVermin(vermin: VerminType, count: Int = 1) {
        tracker.modify(SkyHanniTracker.DisplayMode.TOTAL) { it.count.addOrPut(vermin, count) }
    }

    private fun setVermin(vermin: VerminType, count: Int) {
        tracker.modify(SkyHanniTracker.DisplayMode.TOTAL) { it.count[vermin] = count }
    }

    private fun drawDisplay(data: Data): List<List<Any>> = buildList {
        addAsSingletonList("§7Vermin Tracker:")
        data.count.entries.sortedBy { it.key.order }.forEach { (vermin, amount) ->
            val verminName = vermin.vermin
            addAsSingletonList(" §7- §e${amount.addSeparators()} $verminName")
        }
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return
        if (!config.showOutsideWestVillage && !IslandArea.INFESTED_HOUSE.isInside() && !IslandArea.WEST_VILLAGE.isInside()) return
        if (!config.showWithoutVacuum && !hasVacuum) return

        tracker.renderDisplay(config.position)
    }

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (event.newIsland == IslandType.THE_RIFT) {
            tracker.firstUpdate()
        }
    }

    fun resetCommand() {
        tracker.resetCommand()
    }

    private fun isEnabled() = RiftAPI.inRift() && config.enabled
}
