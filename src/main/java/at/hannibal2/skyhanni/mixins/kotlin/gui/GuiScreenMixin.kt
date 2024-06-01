package at.hannibal2.skyhanni.mixins.kotlin.gui

import at.hannibal2.skyhanni.data.ToolTipData
import at.hannibal2.skyhanni.events.render.gui.RenderItemTooltipEvent
import at.hannibal2.skyhanni.kmixin.annotations.InjectionKind
import at.hannibal2.skyhanni.kmixin.annotations.KInject
import at.hannibal2.skyhanni.kmixin.annotations.KInjectAt
import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import at.hannibal2.skyhanni.kmixin.annotations.TargetShift
import net.minecraft.client.gui.GuiScreen
import net.minecraft.item.ItemStack
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@KMixin(GuiScreen::class)
object GuiScreenMixin {

    @KInject(method = "renderToolTip", kind = InjectionKind.TAIL)
    fun renderToolTip(stack: ItemStack, x: Int, y: Int, ci: CallbackInfo?) {
        RenderItemTooltipEvent(stack).post()
    }

    @KInjectAt(
        method = "renderToolTip",
        target = "Lnet/minecraft/item/ItemStack;getRarity()Lnet/minecraft/item/EnumRarity;",
        shift = TargetShift.AFTER,
        captureLocals = true,
        cancellable = true
    )
    fun getTooltip(stack: ItemStack, x: Int, y: Int, ci: CallbackInfo, list: MutableList<String>) {
        ToolTipData.getTooltip(stack, list)
        if (list.isEmpty()) {
            ci.cancel()
        }
    }
}
