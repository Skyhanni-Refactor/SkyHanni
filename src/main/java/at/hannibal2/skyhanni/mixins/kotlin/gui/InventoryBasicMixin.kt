package at.hannibal2.skyhanni.mixins.kotlin.gui

import at.hannibal2.skyhanni.events.render.gui.ReplaceItemEvent
import at.hannibal2.skyhanni.kmixin.annotations.InjectionKind
import at.hannibal2.skyhanni.kmixin.annotations.KInject
import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import at.hannibal2.skyhanni.kmixin.annotations.KSelf
import at.hannibal2.skyhanni.kmixin.annotations.KShadow
import net.minecraft.inventory.InventoryBasic
import net.minecraft.item.ItemStack
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@KMixin(InventoryBasic::class)
object InventoryBasicMixin {

    @KInject(method = "getStackInSlot", kind = InjectionKind.HEAD, cancellable = true)
    fun getStackInSlot(
        slot: Int,
        cir: CallbackInfoReturnable<ItemStack?>,
        @KSelf self: InventoryBasic,
        @KShadow inventoryContents: Array<ItemStack?>
    ) {
        val originalItem = inventoryContents.getOrNull(slot) ?: return
        val event = ReplaceItemEvent(self, originalItem, slot)
        event.post()
        event.replacement?.let { cir.returnValue = it }
    }
}
