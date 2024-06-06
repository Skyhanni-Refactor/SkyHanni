package at.hannibal2.skyhanni.features.fame

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.IslandArea
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.inventory.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.render.gui.SlotClickEvent
import at.hannibal2.skyhanni.events.utils.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object AccountUpgradeReminder {

    private var inInventory = false
    private var duration: Duration? = null
    private var lastReminderSend = SimpleTimeMark.farPast()

    // TODO make into repo pattern
    private val durationRegex = "§8Duration: (\\d{1,3})d".toRegex()
    private val startedRegex = "§eYou started the §r§a(.+) §r§eupgrade!".toRegex()
    private val claimedRegex = "§eYou claimed the §r§a.+ §r§eupgrade!".toRegex()

    // TODO: find a way to save SimpleTimeMark directly in the config
    private var nextCompletionTime: SimpleTimeMark?
        get() = ProfileStorageData.playerSpecific?.nextAccountUpgradeCompletionTime?.asTimeMark()
        set(value) {
            value?.let {
                ProfileStorageData.playerSpecific?.nextAccountUpgradeCompletionTime = it.toMillis()
            }
        }

    // TODO: Merge this logic with CityProjectFeatures reminder to reduce duplication
    @HandleEvent(onlyOnSkyblock = true)
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        val playerSpecific = ProfileStorageData.playerSpecific ?: return
        if (ReminderUtils.isBusy()) return
        if (IslandArea.COMMUNITY_CENTER.isInside()) return

        val upgrade = playerSpecific.currentAccountUpgrade ?: return
        val nextCompletionTime = nextCompletionTime ?: return
        if (!nextCompletionTime.isInPast()) return
        if (lastReminderSend.passedSince() < 30.seconds) return
        lastReminderSend = SimpleTimeMark.now()

        ChatUtils.clickableChat(
            "The §a$upgrade §eupgrade has completed! §c(Click to disable these reminders)",
            onClick = {
                disable()
            },
            oneTimeClick = true
        )
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        inInventory = event.inventoryName == "Community Shop"
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onSlotClick(event: SlotClickEvent) {
        if (!inInventory) return
        val clickedItemLore = event.slot?.stack?.getLore() ?: return
        if (clickedItemLore.getOrNull(0) != "§8Account Upgrade") return
        val result = clickedItemLore.firstNotNullOfOrNull {
            durationRegex.matchEntire(it)
        } ?: return
        duration = result.groups[1]!!.value.toInt().days
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onChat(event: SkyHanniChatEvent) {
        if (claimedRegex.matches(event.message)) {
            clearUpgrade()
        } else {
            val upgrade = startedRegex.matchEntire(event.message)?.groups?.get(1)?.value ?: return
            startUpgrade(upgrade)
        }
    }

    private fun startUpgrade(upgrade: String) {
        val duration = duration ?: return
        val playerSpecific = ProfileStorageData.playerSpecific ?: return
        playerSpecific.currentAccountUpgrade = upgrade

        nextCompletionTime = SimpleTimeMark.now() + duration
    }

    private fun clearUpgrade() {
        val playerSpecific = ProfileStorageData.playerSpecific ?: return
        playerSpecific.currentAccountUpgrade = null
        nextCompletionTime = SimpleTimeMark.farPast()
    }

    fun disable() {
        SkyHanniMod.feature.misc.accountUpgradeReminder = false
        ChatUtils.chat("Disabled account upgrade reminder.")
    }

    private fun isEnabled() = SkyHanniMod.feature.misc.accountUpgradeReminder
}
