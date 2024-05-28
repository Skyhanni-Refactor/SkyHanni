package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.inventory.InventoryCloseEvent
import at.hannibal2.skyhanni.events.inventory.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.render.gui.BackgroundDrawnEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RegexUtils.anyMatches
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
object TabWidgetSettings {
    private val patternGroup = RepoPattern.group("tab.widget.setting")
    private val mainPageSettingPattern by patternGroup.pattern(
        "gui",
        "(Widgets in.*|Widgets on.*)"
    )
    private val mainPageWidgetPattern by patternGroup.pattern(
        "main",
        "§7Currently:.*"
    )
    private val subPageWidgetPattern by patternGroup.pattern(
        "sub",
        "§eClick to .*"
    )
    private val shownSettingPattern by patternGroup.pattern(
        "show",
        "Shown .* Setting.*|.*Widget Settings"
    )
    private val clickToDisablePattern by patternGroup.pattern(
        "click.disable",
        ".*(disable!)"
    )
    private val enabledPattern by patternGroup.pattern(
        "is.enabled",
        ".*ENABLED"
    )

    var inInventory = false
    var highlights = mutableMapOf<Int, LorenzColor>()

    @HandleEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return
        highlights.clear()

        val inventoryName = event.inventoryName
        if (mainPageSettingPattern.matches(inventoryName)) {
            inInventory = true
            val items = event.inventoryItems.filter { mainPageWidgetPattern.anyMatches(it.value.getLore()) }
            for ((slot, stack) in items) {
                highlights[slot] = if (enabledPattern.anyMatches(stack.getLore())) {
                    LorenzColor.GREEN
                } else {
                    LorenzColor.RED
                }
            }
        }

        if (shownSettingPattern.matches(inventoryName)) {
            inInventory = true
            val items = event.inventoryItems.filter {
                subPageWidgetPattern.matches(it.value.getLore().lastOrNull())
            }

            for ((slot, stack) in items) {
                highlights[slot] = if (clickToDisablePattern.anyMatches(stack.getLore())) {
                    LorenzColor.GREEN
                } else {
                    LorenzColor.RED
                }
            }
        }
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
        highlights.clear()
    }

    @HandleEvent
    fun onBackgroundDrawn(event: BackgroundDrawnEvent) {
        if (!isEnabled()) return
        if (!inInventory) return

        event.gui.inventorySlots.inventorySlots
            .associateWith { highlights[it.slotNumber] }
            .forEach { (slot, color) ->
                color?.let { slot.highlight(it) }
            }
    }

    private fun isEnabled() = SkyBlockAPI.isConnected && SkyHanniMod.feature.inventory.highlightWidgets
}
