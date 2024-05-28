package at.hannibal2.skyhanni.features.garden.contest

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.inventory.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.item.SkyHanniToolTipEvent
import at.hannibal2.skyhanni.events.render.gui.BackgroundDrawnEvent
import at.hannibal2.skyhanni.events.render.gui.GuiRenderItemEvent
import at.hannibal2.skyhanni.events.render.gui.SlotClickEvent
import at.hannibal2.skyhanni.features.garden.GardenNextJacobContest
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.InventoryUtils.getUpperItems
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.drawSlotText
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.datetime.SkyBlockTime
import at.hannibal2.skyhanni.utils.mc.McPlayer
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.system.OS
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import java.text.SimpleDateFormat
import java.util.Locale

@SkyHanniModule
object JacobFarmingContestsInventory {

    private val realTime = mutableMapOf<Int, String>()

    private val formatDay = SimpleDateFormat("dd MMMM yyyy", Locale.US)
    private val formatTime = SimpleDateFormat("HH:mm", Locale.US)
    private val config get() = SkyHanniMod.feature.inventory.jacobFarmingContests

    // Render the contests a tick delayed to feel smoother
    private var hideEverything = true
    private val medalPattern by RepoPattern.pattern(
        "garden.jacob.contests.inventory.medal",
        "§7§7You placed in the (?<medal>.*) §7bracket!"
    )

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        realTime.clear()
        hideEverything = true
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        if (event.inventoryName != "Your Contests") return

        realTime.clear()

        val foundEvents = mutableListOf<String>()
        for ((slot, item) in event.inventoryItems) {
            if (!item.getLore().any { it.startsWith("§7Your score: §e") }) continue

            foundEvents.add(item.name)
            val time = FarmingContestAPI.getSbTimeFor(item.name) ?: continue
            FarmingContestAPI.addContest(time, item)
            if (config.realTime) {
                readRealTime(time, slot)
            }
        }
        hideEverything = false
    }

    private fun readRealTime(time: Long, slot: Int) {
        val dayFormat = formatDay.format(time)
        val startTimeFormat = formatTime.format(time)
        val endTimeFormat = formatTime.format(time + 1000 * 60 * 20)
        realTime[slot] = "$dayFormat $startTimeFormat-$endTimeFormat"
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onSlotClick(event: SlotClickEvent) {
        // TODO add tooltip line "click + press <keybind> to open on elite website
        if (!config.openOnElite.isKeyHeld()) return

        val slot = event.slot ?: return
        val itemName = slot.stack.name

        when (val chestName = InventoryUtils.openInventoryName()) {
            "Your Contests" -> {
                val (year, month, day) = FarmingContestAPI.getSbDateFromItemName(itemName) ?: return
                openContest(year, month, day)
                event.cancel()
            }

            "Jacob's Farming Contests" -> {
                openFromJacobMenu(itemName)
                event.cancel()
            }

            else -> {
                openFromCalendar(chestName, itemName, event, slot)
            }
        }
    }

    private fun openContest(year: String, month: String, day: String) {
        val date = "$year/${LorenzUtils.getSBMonthByName(month)}/$day"
        OS.openUrl("https://elitebot.dev/contests/$date")
        ChatUtils.chat("Opening contest in elitebot.dev")
    }

    private fun openFromJacobMenu(itemName: String) {
        when (itemName) {
            "§6Upcoming Contests" -> {
                OS.openUrl("https://elitebot.dev/contests/upcoming")
                ChatUtils.chat("Opening upcoming contests in elitebot.dev")
            }

            "§bClaim your rewards!" -> {
                OS.openUrl("https://elitebot.dev/@${McPlayer.name}/${SkyBlockAPI.profileName}/contests")
                ChatUtils.chat("Opening your contests in elitebot.dev")
            }

            "§aWhat is this?" -> {
                OS.openUrl("https://elitebot.dev/contests")
                ChatUtils.chat("Opening contest page in elitebot.dev")
            }

            else -> return
        }
    }

    private fun openFromCalendar(
        chestName: String,
        itemName: String,
        event: SlotClickEvent,
        slot: Slot,
    ) {
        GardenNextJacobContest.monthPattern.matchMatcher(chestName) {
            if (!slot.stack.getLore().any { it.contains("§eJacob's Farming Contest") }) return

            val day = GardenNextJacobContest.dayPattern.matchMatcher(itemName) { group("day") } ?: return
            val year = group("year")
            val month = group("month")
            val time = SkyBlockTime(year.toInt(), LorenzUtils.getSBMonthByName(month), day.toInt()).toMillis()
            if (time < SkyBlockTime.now().toMillis()) {
                openContest(year, month, day)
            } else {
                val timestamp = time / 1000
                OS.openUrl("https://elitebot.dev/contests/upcoming#$timestamp")
                ChatUtils.chat("Opening upcoming contests in elitebot.dev")
            }
            event.cancel()
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onBackgroundDrawn(event: BackgroundDrawnEvent) {
        if (!config.highlightRewards) return
        if (!InventoryUtils.openInventoryName().contains("Your Contests")) return

        // hide green border for a tick
        if (hideEverything) return

        if (event.gui !is GuiChest) return
        val guiChest = event.gui
        val chest = guiChest.inventorySlots as ContainerChest

        for ((slot, stack) in chest.getUpperItems()) {
            if (stack.getLore().any { it == "§eClick to claim reward!" }) {
                slot.highlight(LorenzColor.GREEN)
            }
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onTooltip(event: SkyHanniToolTipEvent) {
        if (!InventoryUtils.openInventoryName().contains("Your Contests")) return

        val slot = event.slot.slotNumber
        if (config.realTime) {
            realTime[slot]?.let {
                val toolTip = event.toolTip
                if (toolTip.size > 1) {
                    toolTip.add(1, it)
                }
            }
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onRenderItemOverlayPost(event: GuiRenderItemEvent.RenderOverlayEvent.GuiRenderItemPost) {
        if (!config.medalIcon) return
        if (!InventoryUtils.openInventoryName().contains("Your Contests")) return

        val stack = event.stack ?: return
        var finneganContest = false

        for (line in stack.getLore()) {
            if (line.contains("Contest boosted by Finnegan!")) finneganContest = true

            val name = medalPattern.matchMatcher(line) { group("medal").removeColor() } ?: continue
            val medal = LorenzUtils.enumValueOfOrNull<ContestBracket>(name) ?: return

            var stackTip = "§${medal.color}✦"
            var x = event.x + 9
            var y = event.y + 1
            var scale = .7f

            if (finneganContest && config.finneganIcon) {
                stackTip = "§${medal.color}▲"
                x = event.x + 5
                y = event.y - 2
                scale = 1.3f
            }

            event.drawSlotText(x, y, stackTip, scale)
        }
    }
}
