package at.hannibal2.skyhanni.events.render.gui

import at.hannibal2.skyhanni.api.event.CancellableSkyHanniEvent
import at.hannibal2.skyhanni.utils.GuiRenderUtils
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Container

data class BeforeDrawEvent(
    val gui: GuiContainer,
    val container: Container,
    val mouseX: Int,
    val mouseY: Int,
    val partialTicks: Float,
) : CancellableSkyHanniEvent() {
    fun drawDefaultBackground() =
        GuiRenderUtils.drawGradientRect(0, 0, gui.width, gui.height, -1072689136, -804253680, 0.0)
}
