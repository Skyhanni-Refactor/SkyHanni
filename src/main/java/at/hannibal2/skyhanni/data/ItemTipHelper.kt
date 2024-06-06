package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.skyblock.SkyBlockAPI
import at.hannibal2.skyhanni.events.render.gui.DrawScreenAfterEvent
import at.hannibal2.skyhanni.events.render.gui.GuiRenderItemEvent
import at.hannibal2.skyhanni.events.render.gui.RenderInventoryItemTipEvent
import at.hannibal2.skyhanni.events.render.gui.RenderItemTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.SkyHanniDebugsAndTests
import at.hannibal2.skyhanni.utils.InventoryUtils.getInventoryName
import at.hannibal2.skyhanni.utils.RenderUtils.drawSlotText
import at.hannibal2.skyhanni.utils.mc.McFont
import at.hannibal2.skyhanni.utils.mc.McScreen
import at.hannibal2.skyhanni.utils.mc.McScreen.left
import at.hannibal2.skyhanni.utils.mc.McScreen.top
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest

@SkyHanniModule
object ItemTipHelper {

    @HandleEvent
    fun onRenderItemOverlayPost(event: GuiRenderItemEvent.RenderOverlayEvent.GuiRenderItemPost) {
        val stack = event.stack ?: return
        if (!SkyBlockAPI.isConnected || stack.stackSize != 1) return

        val itemTipEvent = RenderItemTipEvent(stack, mutableListOf())
        itemTipEvent.post()

        if (itemTipEvent.renderObjects.isEmpty()) return

        for (renderObject in itemTipEvent.renderObjects) {
            val text = renderObject.text
            val x = event.x + 17 + renderObject.offsetX
            val y = event.y + 9 + renderObject.offsetY

            event.drawSlotText(x, y, text, 1f)
        }
    }

    @HandleEvent(onlyOnSkyblock = true, priority = HandleEvent.HIGHEST)
    fun onRenderInventoryItemOverlayPost(event: DrawScreenAfterEvent) {
        if (!SkyHanniDebugsAndTests.globalRender) return

        val gui = McScreen.asChest ?: return
        val chest = gui.inventorySlots as ContainerChest
        val inventoryName = chest.getInventoryName()

        GlStateManager.disableLighting()
        GlStateManager.disableDepth()
        GlStateManager.disableBlend()
        for (slot in gui.inventorySlots.inventorySlots) {
            val stack = slot.stack ?: continue

            val itemTipEvent = RenderInventoryItemTipEvent(inventoryName, slot, stack)
            itemTipEvent.post()
            val stackTip = itemTipEvent.stackTip
            if (stackTip.isEmpty()) continue

            val xDisplayPosition = slot.xDisplayPosition
            val yDisplayPosition = slot.yDisplayPosition

            val x = gui.left + xDisplayPosition + 17 + itemTipEvent.offsetX - if (itemTipEvent.alignLeft) {
                McFont.width(stackTip)
            } else {
                0
            }
            val y = gui.top + yDisplayPosition + 9 + itemTipEvent.offsetY

            McFont.draw(stackTip, x.toFloat(), y.toFloat(), 16777215)
        }
        GlStateManager.enableLighting()
        GlStateManager.enableDepth()
    }
}
