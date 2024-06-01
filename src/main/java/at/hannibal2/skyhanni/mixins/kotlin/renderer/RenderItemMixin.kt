package at.hannibal2.skyhanni.mixins.kotlin.renderer

import at.hannibal2.skyhanni.events.render.gui.GuiRenderItemEvent
import at.hannibal2.skyhanni.events.render.gui.RenderGuiItemOverlayEvent
import at.hannibal2.skyhanni.kmixin.annotations.InjectionKind
import at.hannibal2.skyhanni.kmixin.annotations.KInject
import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import at.hannibal2.skyhanni.test.SkyHanniDebugsAndTests
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.entity.RenderItem
import net.minecraft.item.ItemStack
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@KMixin(RenderItem::class)
object RenderItemMixin {

    @KInject(method = "renderItemOverlayIntoGUI", kind = InjectionKind.RETURN)
    fun renderItemOverlayPost(fr: FontRenderer, stack: ItemStack?, x: Int, y: Int, text: String?, ci: CallbackInfo) {
        if (!SkyHanniDebugsAndTests.globalRender) return
        GuiRenderItemEvent.RenderOverlayEvent.GuiRenderItemPost(fr, stack, x, y, text).post()
    }

    @KInject(method = "renderItemIntoGUI", kind = InjectionKind.RETURN)
    fun renderItemReturn(stack: ItemStack?, x: Int, y: Int, ci: CallbackInfo) {
        if (!SkyHanniDebugsAndTests.globalRender) return
        RenderGuiItemOverlayEvent(stack, x, y).post()
    }
}
