package at.hannibal2.skyhanni.mixins

import at.hannibal2.skyhanni.kmixin.KMixin
import net.minecraft.client.Minecraft
import net.minecraft.crash.CrashReport
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@KMixin(Mixin(Minecraft::class))
object TestMixin {

    @Inject(method = ["run"], at = [At("HEAD")])
    @JvmStatic
    fun run(ci: CallbackInfo) {
        println("Hello from Mixin!")
    }

    @Inject(method = ["crashed"], at = [At("HEAD")])
    fun onCrashed(report: CrashReport, ci: CallbackInfo) {
        println("Crashed: ${report.completeReport}")
    }
}