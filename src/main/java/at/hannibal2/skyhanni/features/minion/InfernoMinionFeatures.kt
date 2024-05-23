package at.hannibal2.skyhanni.features.minion

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.data.jsonobjects.repo.InfernoMinionFuelsJson
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.inventory.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.item.SkyHanniToolTipEvent
import at.hannibal2.skyhanni.events.render.gui.SlotClickEvent
import at.hannibal2.skyhanni.events.utils.RepositoryReloadEvent
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

object InfernoMinionFeatures {
    private val config get() = SkyHanniMod.feature.misc.minions
    private val infernoMinionTitlePattern by RepoPattern.pattern(
        "minion.infernominiontitle",
        "Inferno Minion .*"
    )
    private var fuelItemIds = listOf<NEUInternalName>()
    private var inInventory = false

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<InfernoMinionFuelsJson>("InfernoMinionFuels")
        fuelItemIds = data.minionFuels
    }

    @HandleEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        inInventory = infernoMinionTitlePattern.matches(event.inventoryName)
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
    }

    @HandleEvent
    fun onSlotClick(event: SlotClickEvent) {
        if (!SkyBlockAPI.isConnected) return
        if (!config.infernoFuelBlocker) return
        if (!inInventory) return

        val containsFuel =
            NEUInternalName.fromItemNameOrNull(event.container.getSlot(19).stack.name) in fuelItemIds
        if (!containsFuel) return

        if (event.slot?.slotNumber == 19 || event.slot?.slotNumber == 53) {
            if (KeyboardManager.isModifierKeyDown()) return
            event.cancel()
        }
    }

    @HandleEvent
    fun onTooltip(event: SkyHanniToolTipEvent) {
        if (!config.infernoFuelBlocker) return
        if (!inInventory) return

        val containsFuel = NEUInternalName.fromItemNameOrNull(event.itemStack.name) in fuelItemIds
        if (!containsFuel) return

        if (event.slot.slotNumber == 19) {
            event.toolTip.add("")
            event.toolTip.add("§c[SkyHanni] is blocking you from taking this out!")
            event.toolTip.add("  §7(Bypass by holding the ${KeyboardManager.getModifierKeyName()} key)")
        }
        if (event.slot.slotNumber == 53) {
            event.toolTip.add("")
            event.toolTip.add("§c[SkyHanni] is blocking you from picking this minion up!")
            event.toolTip.add("  §7(Bypass by holding the ${KeyboardManager.getModifierKeyName()} key)")
        }
    }
}
