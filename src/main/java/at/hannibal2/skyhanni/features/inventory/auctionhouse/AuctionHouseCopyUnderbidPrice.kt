package at.hannibal2.skyhanni.features.inventory.auctionhouse

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.item.SkyhanniItems
import at.hannibal2.skyhanni.events.inventory.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.render.gui.GuiKeyPressEvent
import at.hannibal2.skyhanni.events.utils.ConfigFixEvent
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.NEUItems.getPrice
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.system.OS

class AuctionHouseCopyUnderbidPrice {

    private val config get() = SkyHanniMod.feature.inventory.auctions

    private val patternGroup = RepoPattern.group("auctions.underbid")
    private val auctionPricePattern by patternGroup.pattern(
        "price",
        "§7(?:Buy it now|Starting bid|Top bid): §6(?<coins>[0-9,]+) coins"
    )
    private val allowedInventoriesPattern by patternGroup.pattern(
        "allowedinventories",
        "Auctions Browser|Manage Auctions|Auctions: \".*\"?"
    )

    @HandleEvent(onlyOnSkyblock = true)
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        if (!config.autoCopyUnderbidPrice) return
        if (!event.fullyOpenedOnce) return
        if (event.inventoryName != "Create BIN Auction") return
        val item = event.inventoryItems[13] ?: return

        val internalName = item.getInternalName()
        if (internalName == SkyhanniItems.NONE()) return

        val price = internalName.getPrice().toLong()
        if (price <= 0) {
            OS.copyToClipboard("")
            return
        }
        val newPrice = price * item.stackSize - 1
        OS.copyToClipboard("$newPrice")
        ChatUtils.chat("Copied ${newPrice.addSeparators()} to clipboard. (Copy Underbid Price)")
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onKeybind(event: GuiKeyPressEvent) {
        if (!config.copyUnderbidKeybind.isKeyHeld()) return
        if (!allowedInventoriesPattern.matches(InventoryUtils.openInventoryName())) return
        val stack = event.guiContainer.slotUnderMouse?.stack ?: return

        stack.getLore().matchFirst(auctionPricePattern) {
            val underbid = group("coins").formatLong() - 1
            OS.copyToClipboard("$underbid")
            ChatUtils.chat("Copied ${underbid.addSeparators()} to clipboard.")
        }
    }

    @HandleEvent
    fun onConfigFix(event: ConfigFixEvent) {
        event.move(25, "inventory.copyUnderbidPrice", "inventory.auctions.autoCopyUnderbidPrice")
    }
}
