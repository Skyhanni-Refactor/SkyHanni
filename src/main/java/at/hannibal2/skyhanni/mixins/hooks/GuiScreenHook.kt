package at.hannibal2.skyhanni.mixins.hooks

import at.hannibal2.skyhanni.events.render.gui.RenderItemTooltipEvent
import net.minecraft.item.ItemStack

fun renderToolTip(stack: ItemStack) {
    RenderItemTooltipEvent(stack).post()
}
