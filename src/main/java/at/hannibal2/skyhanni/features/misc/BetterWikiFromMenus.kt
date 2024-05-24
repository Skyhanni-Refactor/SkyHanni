package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.render.gui.SlotClickEvent
import at.hannibal2.skyhanni.features.commands.WikiManager
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.StringUtils.removeColor

object BetterWikiFromMenus {

    private val config get() = SkyHanniMod.feature.misc.commands.betterWiki

    @HandleEvent(onlyOnSkyblock = true, priority = HandleEvent.HIGH)
    fun onSlotClick(event: SlotClickEvent) {
        if (!isEnabled()) return

        val chestName = InventoryUtils.openInventoryName()

        if (chestName.isEmpty()) return

        val itemClickedStack = event.slot?.stack ?: return
        val itemClickedName = itemClickedStack.displayName

        val isWiki = event.slotId == 11 && itemClickedName.contains("Wiki Command")
        val isWikithis = event.slotId == 15 && itemClickedName.contains("Wikithis Command")
        val inBiblioInventory = chestName == "SkyBlock Wiki" && (isWiki || isWikithis)
        val inSBGuideInventory =
            (itemClickedStack.getLore().let { it.any { line -> line == "§7§eClick to view on the SkyBlock Wiki!" } })

        if (inBiblioInventory) {
            when {
                isWiki -> WikiManager.sendWikiMessage(useFandom = false)
                else -> WikiManager.otherWikiCommands(arrayOf(""), true, false)
            }
            return
        }

        if (inSBGuideInventory && config.sbGuide) {
            val wikiSearch = itemClickedName.removeColor().replace("✔ ", "").replace("✖ ", "")
            WikiManager.sendWikiMessage(wikiSearch, autoOpen = config.menuOpenWiki)
            event.cancel()
        }
    }

    private fun isEnabled() = config.enabled
}
