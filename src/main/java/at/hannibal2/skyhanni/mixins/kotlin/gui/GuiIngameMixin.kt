package at.hannibal2.skyhanni.mixins.kotlin.gui

import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import at.hannibal2.skyhanni.kmixin.annotations.KRedirectCall
import at.hannibal2.skyhanni.mixins.hooks.drawString
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.GuiIngame

@KMixin(GuiIngame::class)
object GuiIngameMixin {

    @KRedirectCall(
        method = "renderScoreboard",
        target = "Lnet/minecraft/client/gui/FontRenderer;drawString(Ljava/lang/String;III)I"
    )
    fun renderItemOverlayPost(instance: FontRenderer?, text: String?, x: Int, y: Int, color: Int): Int {
        return drawString(instance!!, text!!, x, y, color)
    }
}
