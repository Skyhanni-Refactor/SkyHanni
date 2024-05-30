package at.hannibal2.skyhanni.kmixin

import org.spongepowered.asm.mixin.Mixin

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KMixin(
    val value: Mixin
)
