package at.hannibal2.skyhanni.kmixin.annotations

import com.squareup.javapoet.ClassName

val MIXIN_CLASS = ClassName.get("org.spongepowered.asm.mixin", "Mixin")
val SHADOW_CLASS = ClassName.get("org.spongepowered.asm.mixin", "Shadow")
val FINAL_CLASS = ClassName.get("org.spongepowered.asm.mixin", "Final")
val AT_CLASS = ClassName.get("org.spongepowered.asm.mixin.injection", "At")
val INJECT_CLASS = ClassName.get("org.spongepowered.asm.mixin.injection", "Inject")
val REDIRECT_CLASS = ClassName.get("org.spongepowered.asm.mixin.injection", "Redirect")
