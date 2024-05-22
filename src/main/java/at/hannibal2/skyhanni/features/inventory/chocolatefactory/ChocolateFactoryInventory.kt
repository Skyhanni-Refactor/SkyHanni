package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.render.gui.BackgroundDrawnEvent
import at.hannibal2.skyhanni.events.render.gui.ForegroundDrawnEvent
import at.hannibal2.skyhanni.events.render.gui.RenderInventoryItemTipEvent
import at.hannibal2.skyhanni.events.render.gui.SlotClickEvent
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RenderUtils.drawSlotText
import at.hannibal2.skyhanni.utils.RenderUtils.highlight

object ChocolateFactoryInventory {

    private val config get() = ChocolateFactoryAPI.config

    private val unclaimedRewardsPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "unclaimedrewards",
        "§7§aYou have \\d+ unclaimed rewards?!"
    )

    @HandleEvent
    fun onForegroundDrawn(event: ForegroundDrawnEvent) {
        if (!ChocolateFactoryAPI.inChocolateFactory) return
        if (!config.highlightUpgrades) return


        for (slot in InventoryUtils.getItemsInOpenChest()) {
            if (slot.stack == null) continue
            val slotIndex = slot.slotNumber

            if (slotIndex == ChocolateFactoryAPI.bestPossibleSlot) {
                event.drawSlotText(slot.xDisplayPosition + 18, slot.yDisplayPosition, "§6✦", 1f)
            }
        }
    }

    @HandleEvent
    fun onBackgroundDrawn(event: BackgroundDrawnEvent) {
        if (!ChocolateFactoryAPI.inChocolateFactory) return
        if (!config.highlightUpgrades) return

        for (slot in InventoryUtils.getItemsInOpenChest()) {
            if (slot.stack == null) continue
            val slotIndex = slot.slotNumber

            val currentUpdates = ChocolateFactoryAPI.factoryUpgrades
            currentUpdates.find { it.slotIndex == slotIndex }?.let { upgrade ->
                if (upgrade.canAfford()) {
                    slot highlight LorenzColor.GREEN.addOpacity(75)
                }
            }
            if (slotIndex == ChocolateFactoryAPI.bestAffordableSlot) {
                slot highlight LorenzColor.GREEN.addOpacity(200)
            }

            if (slotIndex == ChocolateFactoryAPI.barnIndex && ChocolateFactoryBarnManager.barnFull) {
                slot highlight LorenzColor.RED
            }
            if (slotIndex == ChocolateFactoryAPI.clickRabbitSlot) {
                slot highlight LorenzColor.RED
            }
            if (slotIndex == ChocolateFactoryAPI.milestoneIndex) {
                slot.stack?.getLore()?.matchFirst(unclaimedRewardsPattern) {
                    slot highlight LorenzColor.RED
                }
            }
            if (slotIndex == ChocolateFactoryAPI.timeTowerIndex) {
                if (ChocolateFactoryTimeTowerManager.timeTowerActive()) {
                    slot highlight LorenzColor.LIGHT_PURPLE.addOpacity(200)
                }
                if (ChocolateFactoryTimeTowerManager.timeTowerFull()) {
                    slot highlight LorenzColor.RED
                }
            }
        }
    }

    @HandleEvent
    fun onRenderItemTip(event: RenderInventoryItemTipEvent) {
        if (!ChocolateFactoryAPI.inChocolateFactory) return
        if (!config.showStackSizes) return

        val upgradeInfo = ChocolateFactoryAPI.factoryUpgrades.find { it.slotIndex == event.slot.slotNumber } ?: return
        event.stackTip = upgradeInfo.stackTip()
    }

    @HandleEvent
    fun onSlotClick(event: SlotClickEvent) {
        if (!ChocolateFactoryAPI.inChocolateFactory) return
        val slot = event.slot ?: return
        val slotNumber = slot.slotNumber
        if (!config.useMiddleClick) return
        if (slotNumber in ChocolateFactoryAPI.noPickblockSlots &&
            (slotNumber != ChocolateFactoryAPI.timeTowerIndex || event.clickedButton == 1)
        ) return

        // this would break ChocolateFactoryKeybinds otherwise
        if (event.clickTypeEnum == SlotClickEvent.ClickType.HOTBAR) return

        event.makePickblock()
    }
}
