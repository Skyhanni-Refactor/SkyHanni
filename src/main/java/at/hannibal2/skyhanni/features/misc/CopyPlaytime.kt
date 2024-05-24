package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.item.SkyHanniToolTipEvent
import at.hannibal2.skyhanni.events.render.gui.SlotClickEvent
import at.hannibal2.skyhanni.features.misc.limbo.LimboPlaytime
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.mc.McPlayer
import at.hannibal2.skyhanni.utils.system.OS

object CopyPlaytime {
    @HandleEvent(priority = HandleEvent.LOWEST)
    fun onTooltip(event: SkyHanniToolTipEvent) {
        if (InventoryUtils.openInventoryName() != "Detailed /playtime") return
        if (event.slot.slotNumber != 4) return

        event.toolTip.add("")
        event.toolTip.add("§eClick to Copy!")
    }

    @HandleEvent
    fun onSlotClicked(event: SlotClickEvent) {
        if (InventoryUtils.openInventoryName() != "Detailed /playtime") return
        if (event.slotId != 4) return
        if (event.clickedButton != 0) return

        event.cancel()
        val text = LimboPlaytime.tooltipPlaytime.dropLast(2).toMutableList()

        val profile = SkyBlockAPI.profileName
        text.add(0, "${McPlayer.name}'s - $profile Playtime Stats")

        OS.copyToClipboard(text.joinToString("\n") { it.removeColor() })
        ChatUtils.chat("Copied playtime stats into clipboard.")
    }
}
