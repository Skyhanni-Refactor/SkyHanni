package at.hannibal2.skyhanni.mixins.kotlin

import at.hannibal2.skyhanni.features.garden.farming.GardenCustomKeybinds.isKeyDown
import at.hannibal2.skyhanni.features.garden.farming.GardenCustomKeybinds.onTick
import at.hannibal2.skyhanni.kmixin.annotations.InjectionKind
import at.hannibal2.skyhanni.kmixin.annotations.KInject
import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import at.hannibal2.skyhanni.kmixin.annotations.KSelf
import at.hannibal2.skyhanni.kmixin.annotations.KStatic
import net.minecraft.client.settings.KeyBinding
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@KMixin(KeyBinding::class)
object KeyBindingMixin {

    @KStatic
    @KInject(method = "onTick", kind = InjectionKind.HEAD, cancellable = true)
    fun noOnTick(keyCode: Int, ci: CallbackInfo) {
        onTick(keyCode, ci)
    }

    @KInject(method = "isKeyDown", kind = InjectionKind.HEAD, cancellable = true)
    fun noIsKeyDown(cir: CallbackInfoReturnable<Boolean>, @KSelf self: KeyBinding) {
        isKeyDown(self, cir)
    }
}
