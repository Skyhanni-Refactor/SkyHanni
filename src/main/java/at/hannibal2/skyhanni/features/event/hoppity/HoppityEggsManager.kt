package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.render.gui.GuiOverlayRenderEvent
import at.hannibal2.skyhanni.events.utils.ConfigFixEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.features.fame.ReminderUtils
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.fromNow
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.now
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.datetime.SkyBlockTime
import at.hannibal2.skyhanni.utils.datetime.TimeUtils.format
import at.hannibal2.skyhanni.utils.mc.McSound
import at.hannibal2.skyhanni.utils.mc.McSound.playOnRepeat
import java.util.regex.Matcher
import kotlin.time.Duration.Companion.seconds

object HoppityEggsManager {

    val config get() = SkyHanniMod.feature.event.hoppityEggs

    /**
     * REGEX-TEST: §d§lHOPPITY'S HUNT §r§dYou found a §r§9Chocolate Lunch Egg §r§don a ledge next to the stairs up§r§d!
     * REGEX-TEST: §d§lHOPPITY'S HUNT §r§dYou found a §r§aChocolate Dinner Egg §r§dbehind Emissary Sisko§r§d!
     * REGEX-TEST: §d§lHOPPITY'S HUNT §r§dYou found a §r§9Chocolate Lunch Egg §r§dnear the Diamond Essence Shop§r§d!
     */
    private val eggFoundPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "egg.found",
        "§d§lHOPPITY'S HUNT §r§dYou found a §r§.Chocolate (?<meal>\\w+) Egg §r§d(?<note>.*)§r§d!"
    )
    private val noEggsLeftPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "egg.noneleft",
        "§cThere are no hidden Chocolate Rabbit Eggs nearby! Try again later!"
    )
    private val eggSpawnedPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "egg.spawned",
        "§d§lHOPPITY'S HUNT §r§dA §r§.Chocolate (?<meal>\\w+) Egg §r§dhas appeared!"
    )
    private val eggAlreadyCollectedPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "egg.alreadycollected",
        "§cYou have already collected this Chocolate (?<meal>\\w+) Egg§r§c! Try again when it respawns!"
    )
    private val hoppityEventNotOn by ChocolateFactoryAPI.patternGroup.pattern(
        "egg.notevent",
        "§cThis only works during Hoppity's Hunt!"
    )

    private var lastMeal: HoppityEggType? = null
    private var lastNote: String? = null

    // has claimed all eggs at least once
    private var warningActive = false
    private var lastWarnTime = SimpleTimeMark.farPast()

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        lastMeal = null
        lastNote = null
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onChat(event: SkyHanniChatEvent) {
        hoppityEventNotOn.matchMatcher(event.message) {
            val currentYear = SkyBlockTime.now().year

            if (config.timeInChat) {
                val timeUntil = SkyBlockTime(currentYear + 1).asTimeMark().timeUntil()
                ChatUtils.chat("§eHoppity's Hunt is not active. The next Hoppity's Hunt is in §b${timeUntil.format()}§e.")
                event.blockedReason = "hoppity_egg"
            }
            return
        }

        if (!ChocolateFactoryAPI.isHoppityEvent()) return

        eggFoundPattern.matchMatcher(event.message) {
            HoppityEggLocator.eggFound()
            val meal = getEggType(event)
            val note = group("note").removeColor()
            meal.markClaimed()
            lastMeal = meal
            lastNote = note
            return
        }

        noEggsLeftPattern.matchMatcher(event.message) {
            HoppityEggType.allFound()

            if (config.timeInChat) {
                val nextEgg = HoppityEggType.entries.minByOrNull { it.timeUntil() } ?: return
                ChatUtils.chat("§eNext egg available in §b${nextEgg.timeUntil().format()}§e.")
                event.blockedReason = "hoppity_egg"
            }
            return
        }

        eggAlreadyCollectedPattern.matchMatcher(event.message) {
            getEggType(event).markClaimed()
            if (config.timeInChat) {
                val nextEgg = HoppityEggType.entries.minByOrNull { it.timeUntil() } ?: return
                ChatUtils.chat("§eNext egg available in §b${nextEgg.timeUntil().format()}§e.")
                event.blockedReason = "hoppity_egg"
            }
            return
        }

        eggSpawnedPattern.matchMatcher(event.message) {
            getEggType(event).markSpawned()
            return
        }
    }

    internal fun Matcher.getEggType(event: SkyHanniChatEvent): HoppityEggType =
        HoppityEggType.getMealByName(group("meal")) ?: run {
            ErrorManager.skyHanniError(
                "Unknown meal: ${group("meal")}",
                "message" to event.message
            )
        }

    fun shareWaypointPrompt() {
        if (!config.sharedWaypoints) return
        val meal = lastMeal ?: return
        val note = lastNote ?: return
        lastMeal = null
        lastNote = null

        val currentLocation = LocationUtils.playerLocation()
        DelayedRun.runNextTick {
            ChatUtils.clickableChat(
                "Click here to share the location of this chocolate egg with the server!",
                onClick = { HoppityEggsShared.shareNearbyEggLocation(currentLocation, meal, note) },
                expireAt = 30.seconds.fromNow(),
                oneTimeClick = true
            )
        }
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiOverlayRenderEvent) {
        if (!isActive()) return
        if (!config.showClaimedEggs) return
        if (isBusy()) return
        if (!ChocolateFactoryAPI.isHoppityEvent()) return

        val displayList = HoppityEggType.entries
            .map { "§7 - ${it.formattedName} ${it.timeUntil().format()}" }
            .toMutableList()
        displayList.add(0, "§bUnfound Eggs:")
        if (displayList.size == 1) return

        config.position.renderStrings(displayList, posLabel = "Hoppity Eggs")
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isActive()) return
        HoppityEggType.checkClaimed()
        checkWarn()
    }

    private fun checkWarn() {
        if (!warningActive) {
            warningActive = HoppityEggType.entries.all { it.isClaimed() }
        }

        if (warningActive) {
            if (HoppityEggType.entries.all { !it.isClaimed() }) {
                warn()
            }
        }
    }

    private fun warn() {
        if (!config.warnUnclaimedEggs) return
        if (isBusy()) return
        if (lastWarnTime.passedSince() < 30.seconds) return

        lastWarnTime = now()
        val amount = HoppityEggType.entries.size
        val message = "All $amount Hoppity Eggs are ready to be found!"
        if (config.warpUnclaimedEggs) {
            ChatUtils.clickableChat(
                message,
                onClick = { HypixelCommands.warp(config.warpDestination) },
                "§eClick to /warp ${config.warpDestination}!"
            )
        } else ChatUtils.chat(message)
        TitleManager.sendTitle("§e$amount Hoppity Eggs!", 5.seconds)
        McSound.PLING.playOnRepeat(100, 10)
    }

    private fun isBusy() = ReminderUtils.isBusy(config.showDuringContest)

    @HandleEvent
    fun onConfigFix(event: ConfigFixEvent) {
        event.move(
            44,
            "event.chocolateFactory.highlightHoppityShop",
            "event.chocolateFactory.hoppityEggs.highlightHoppityShop"
        )
        event.move(44, "event.chocolateFactory.hoppityEggs", "event.hoppityEggs")
    }

    fun isActive() = SkyBlockAPI.isConnected && ChocolateFactoryAPI.isHoppityEvent()
}
