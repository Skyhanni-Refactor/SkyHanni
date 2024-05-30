package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.inventory.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.inventory.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.render.gui.BackgroundDrawnEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RenderUtils.highlight

@SkyHanniModule
object HoppityNpc {

    private val config get() = HoppityEggsManager.config

    private var lastReminderSent = SimpleTimeMark.farPast()
    private var hoppityYearOpened
        get() = ChocolateFactoryAPI.profileStorage?.hoppityShopYearOpened ?: -1
        set(value) {
            ChocolateFactoryAPI.profileStorage?.hoppityShopYearOpened = value
        }

    private var slotsToHighlight = mutableSetOf<Int>()
    private var inShop = false

    @HandleEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (event.inventoryName != "Hoppity") return
        // TODO maybe we could add an annoying chat message that tells you how many years you have skipped
        //  or the last year you have opened the shop before.
        //  that way we verbally punish non active users in a funny and non harmful way
        hoppityYearOpened = SkyBlockTime.now().year
        inShop = true
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isReminderEnabled()) return
        if (ReminderUtils.isBusy()) return
        if (hoppityYearOpened == SkyBlockTime.now().year) return
        if (!ChocolateFactoryAPI.isHoppityEvent()) return
        if (lastReminderSent.passedSince() <= 30.seconds) return

        ChatUtils.clickableChat(
            "New rabbits are available at §aHoppity's Shop§e! §c(Click to disable this reminder)",
            onClick = {
                disableReminder()
                ChatUtils.chat("§eHoppity's Shop reminder disabled.")
            },
            oneTimeClick = true
        )

        lastReminderSent = SimpleTimeMark.now()
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        clear()
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        clear()
    }

    @HandleEvent
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        if (!inShop) return
        slotsToHighlight.clear()
        for ((slot, item) in event.inventoryItems) {
            if (item.getLore().contains("§eClick to trade!")) {
                slotsToHighlight.add(slot)
            }
        }
    }

    @HandleEvent
    fun onBackgroundDrawn(event: BackgroundDrawnEvent) {
        if (!isHighlightEnabled()) return
        if (!inShop) return
        for (slot in InventoryUtils.getItemsInOpenChest()) {
            if (slot.slotIndex in slotsToHighlight) {
                slot.highlight(LorenzColor.GREEN.addOpacity(200))
            }
        }
    }

    private fun isHighlightEnabled() = LorenzUtils.inSkyBlock && config.highlightHoppityShop
    private fun isReminderEnabled() = LorenzUtils.inSkyBlock && config.hoppityShopReminder

    private fun clear() {
        inShop = false
        slotsToHighlight.clear()
    }

    private fun disableReminder() {
        config.hoppityShopReminder = false
    }
}
