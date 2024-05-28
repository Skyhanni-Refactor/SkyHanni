package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.item.SkyHanniToolTipEvent
import at.hannibal2.skyhanni.events.render.gui.GuiRenderItemEvent
import at.hannibal2.skyhanni.events.utils.ConfigFixEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.RenderUtils.drawSlotText
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getAppliedPocketSackInASack

@SkyHanniModule
object PocketSackInASackDisplay {

    private val config get() = SkyHanniMod.feature.inventory.pocketSackInASack
    private const val MAX_STITCHES = 3

    @HandleEvent
    fun onRenderItemOverlayPost(event: GuiRenderItemEvent.RenderOverlayEvent.GuiRenderItemPost) {
        val stack = event.stack ?: return
        if (!SkyBlockAPI.isConnected || stack.stackSize != 1) return
        if (!config.showOverlay) return
        val pocketSackInASackApplied = stack.getAppliedPocketSackInASack() ?: return

        val stackTip = "§a$pocketSackInASackApplied"
        val x = event.x + 13
        val y = event.y + 1

        event.drawSlotText(x, y, stackTip, .9f)
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onTooltip(event: SkyHanniToolTipEvent) {
        if (!config.replaceLore) return
        val itemStack = event.itemStack
        val applied = itemStack.getAppliedPocketSackInASack() ?: return

        if (!ItemUtils.isSack(itemStack)) return
        val iterator = event.toolTip.listIterator()
        var next = false
        for (line in iterator) {
            if (line.contains("7This sack is")) {
                val color = if (applied == MAX_STITCHES) "§a" else "§b"
                iterator.set("§7This sack is stitched $color$applied§7/$color$MAX_STITCHES")
                next = true
                continue
            }
            if (next) {
                iterator.set("§7times with a §cPocket Sack-in-a-Sack§7.")
                return
            }
        }
    }

    @HandleEvent
    fun onConfigFix(event: ConfigFixEvent) {
        event.move(31, "misc.pocketSackInASack", "inventory.pocketSackInASack")
    }
}
