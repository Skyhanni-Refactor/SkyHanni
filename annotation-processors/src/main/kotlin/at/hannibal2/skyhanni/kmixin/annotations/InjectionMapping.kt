package at.hannibal2.skyhanni.kmixin.annotations

import at.hannibal2.skyhanni.kmixin.injectors.InjectSerializer
import at.hannibal2.skyhanni.kmixin.injectors.InjectionSerializer
import at.hannibal2.skyhanni.kmixin.toJava
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.javapoet.AnnotationSpec
import kotlin.reflect.KClass

object InjectionMapping {

    private val injections = mapOf<KClass<*>, KSAnnotation.() -> AnnotationSpec>(
        KInject::class to {
            AnnotationSpec.builder(INJECT_CLASS)
                .addMember("method", "\"${getAsString("method")}\"")
                .addMember("at", "@\$T(value = \"${getAsInjectionKind("kind").name}\")", AT_CLASS)
                .apply {
                    if (getAsBoolean("cancellable")) {
                        addMember("cancellable", "true")
                    }
                    if (getAsBoolean("captureLocals")) {
                        addMember("locals", "org.spongepowered.asm.mixin.injection.callback.LocalCapture.CAPTURE_FAILHARD")
                    }
                }
                .build()
        }
    )

    private val serializers = mapOf<KClass<*>, InjectionSerializer>(
        KInject::class to InjectSerializer
    )

    fun KSFunctionDeclaration.getInjection(): Pair<AnnotationSpec, InjectionSerializer>? {
        val annotations = this.annotations.associateBy { it.annotationType.resolve().declaration.qualifiedName!!.asString() }
        for ((klass, mapper) in injections) {
            val annotation = annotations[klass.qualifiedName] ?: continue
            return annotation.mapper() to serializers[klass]!!
        }
        return null
    }

    fun KSClassDeclaration.getMixinAnnotation(): AnnotationSpec {
        val annotation = this.annotations.first {
            it.annotationType.resolve().declaration.qualifiedName!!.asString() == KMixin::class.qualifiedName
        }
        return with(annotation) {
            val value = getAsClass("value")
            val priority = getAsInt("priority")
            val remap = getAsBoolean("remap")

            AnnotationSpec.builder(MIXIN_CLASS)
                .addMember("value", "\$T.class", value.toJava())
                .addMember("priority", "\$L", priority)
                .addMember("remap", "\$L", remap)
                .build()
        }
    }
}
