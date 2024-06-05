package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.events.render.gui.ChestGuiOverlayRenderEvent
import at.hannibal2.skyhanni.events.render.gui.GuiOverlayRenderEvent
import at.hannibal2.skyhanni.events.render.gui.GuiRenderEvent
import at.hannibal2.skyhanni.features.misc.visualwords.VisualWordGui
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.SkyHanniDebugsAndTests
import at.hannibal2.skyhanni.utils.mc.McScreen
import at.hannibal2.skyhanni.utils.mc.McScreen.isChest
import at.hannibal2.skyhanni.utils.mc.McScreen.isInventory
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object RenderData {

    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent.Pre) {
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR) return
        if (!canRender()) return
        if (!SkyHanniDebugsAndTests.globalRender) return
        if (GuiEditManager.isInGui() || VisualWordGui.isInGui()) return

        GlStateManager.translate(0f, 0f, -3f)
        GuiRenderEvent().post()
        GuiOverlayRenderEvent().post()
        GlStateManager.translate(0f, 0f, 3f)
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiScreenEvent.BackgroundDrawnEvent) {
        if (!canRender()) return
        if (!SkyHanniDebugsAndTests.globalRender) return
        if (GuiEditManager.isInGui() || VisualWordGui.isInGui()) return
        val currentScreen = McScreen.screen ?: return
        if (!currentScreen.isInventory && !currentScreen.isChest) return

        GlStateManager.pushMatrix()
        GlStateManager.enableDepth()

        if (GuiEditManager.isInGui()) {
            GlStateManager.translate(0f, 0f, -3f)
            GuiOverlayRenderEvent().post()
            GlStateManager.translate(0f, 0f, 3f)
        }

        ChestGuiOverlayRenderEvent().post()

        GlStateManager.popMatrix()
    }

    private fun canRender(): Boolean = Minecraft.getMinecraft()?.renderManager?.fontRenderer != null
}
