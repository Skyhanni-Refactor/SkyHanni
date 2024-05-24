package at.hannibal2.skyhanni.features.event.winter

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.inventory.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.render.gui.GuiOverlayRenderEvent
import at.hannibal2.skyhanni.events.skyblock.IslandChangeEvent
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.datetime.DateUtils
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

object UniqueGiftCounter {

    private val config get() = SkyHanniMod.feature.event.winter.uniqueGiftCounter
    private val storage get() = ProfileStorageData.playerSpecific?.winter

    private val giftedAmountPattern by RepoPattern.pattern(
        "event.winter.uniquegifts.counter.amount",
        "§7Unique Players Gifted: §a(?<amount>.*)"
    )

    private var display = ""

    @HandleEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (event.inventoryName != "Generow") return
        val item = event.inventoryItems[40] ?: return

        val storage = storage ?: return

        item.getLore().matchFirst(giftedAmountPattern) {
            val amount = group("amount").formatInt()
            storage.amountGifted = amount
            update()
        }
    }

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        update()
    }

    fun addUniqueGift() {
        val storage = storage ?: return
        storage.amountGifted++
        update()
    }

    private fun update() {
        val storage = storage ?: return

        val amountGifted = storage.amountGifted
        val max = 600
        val hasMax = amountGifted >= max
        val color = if (hasMax) "§a" else "§e"
        display = "§7Unique Players Gifted: $color$amountGifted/$max"
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiOverlayRenderEvent) {
        if (!isEnabled()) return

        config.position.renderString(
            display,
            posLabel = "Unique Gift Counter"
        )
    }

    private fun isEnabled() = SkyBlockAPI.isConnected && config.enabled && DateUtils.isDecember() &&
        InventoryUtils.itemInHandId.endsWith("_GIFT")
}
