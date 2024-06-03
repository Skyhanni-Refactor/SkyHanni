package at.hannibal2.skyhanni.features.gui

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.inventory.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.render.gui.GuiOverlayRenderEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark
import at.hannibal2.skyhanni.utils.datetime.TimeUtils
import at.hannibal2.skyhanni.utils.datetime.TimeUtils.format
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
object BeaconPower {

    private val storage get() = ProfileStorageData.profileSpecific?.beaconPower
    private val config get() = SkyHanniMod.feature.gui

    private val group = RepoPattern.group("gui.beaconpower")

    // TODO add regex tests
    private val deactivatedPattern by group.pattern(
        "deactivated",
        "§7Beacon Deactivated §8- §cNo Power Remaining"
    )
    private val timeRemainingPattern by group.pattern(
        "time",
        "§7Power Remaining: §e(?<time>.+)"
    )
    private val boostedStatPattern by group.pattern(
        "stat",
        "§7Current Stat: (?<stat>.+)"
    )
    private val noBoostedStatPattern by group.pattern(
        "nostat",
        "TODO"
    )

    private var expiryTime: SimpleTimeMark
        get() = storage?.beaconPowerExpiryTime?.asTimeMark() ?: SimpleTimeMark.farPast()
        set(value) {
            storage?.beaconPowerExpiryTime = value.toMillis()
        }

    private var stat: String?
        get() = storage?.boostedStat
        set(value) {
            storage?.boostedStat = value
        }

    private var display: String? = null

    private val BEACON_POWER_SLOT = 22
    private val STATS_SLOT = 23

    @HandleEvent
    fun onInventoryUpdate(event: InventoryUpdatedEvent) {
        if (!SkyBlockAPI.isConnected) return
        if (event.inventoryName != "Beacon") return
        val items = event.inventoryItems

        items[BEACON_POWER_SLOT]?.let { item ->
            item.getLore().forEach {
                if (deactivatedPattern.matches(it)) {
                    expiryTime = SimpleTimeMark.farPast()
                    return@let
                }
                timeRemainingPattern.matchMatcher(it) {
                    val duration = TimeUtils.getDuration(group("time"))
                    expiryTime = SimpleTimeMark.now() + duration
                    return@let
                }
            }
        }

        items[STATS_SLOT]?.let { item ->
            item.getLore().forEach {
                if (noBoostedStatPattern.matches(it)) {
                    stat = null
                    return@let
                }
                boostedStatPattern.matchMatcher(it) {
                    stat = group("stat")
                    return@let
                }
            }
        }
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        val string = display ?: return
        config.beaconPowerPosition.renderString(string, posLabel = "Beacon Power")
    }

    @HandleEvent
    fun onSecond(event: SecondPassedEvent) {
        if (!isEnabled()) return
        display = drawDisplay()
    }

    private fun drawDisplay(): String = buildString {
        append("§eBeacon: ")
        if (expiryTime.isInPast()) {
            append("§cNot active")
        } else {
            append("§b${expiryTime.timeUntil().format(maxUnits = 2)}")
            if (config.beaconPowerStat) append(" §7(${stat ?: "§cNo stat"}§7)")
        }
    }


    private fun isEnabled() = SkyBlockAPI.isConnected && config.beaconPower
}
