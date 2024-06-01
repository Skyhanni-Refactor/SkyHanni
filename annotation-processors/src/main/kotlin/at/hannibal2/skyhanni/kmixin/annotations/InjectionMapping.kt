package at.hannibal2.skyhanni.kmixin.annotations

import at.hannibal2.skyhanni.kmixin.injectors.InjectAtSerializer
import at.hannibal2.skyhanni.kmixin.injectors.InjectSerializer
import at.hannibal2.skyhanni.kmixin.injectors.InjectionSerializer
import at.hannibal2.skyhanni.kmixin.injectors.RedirectMethodSerializer
import at.hannibal2.skyhanni.kmixin.toJava
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.javapoet.AnnotationSpec
import kotlin.reflect.KClass

object InjectionMapping {

    private val serializers = mapOf<KClass<*>, InjectionSerializer>(
        KInject::class to InjectSerializer,
        KInjectAt::class to InjectAtSerializer,
        KRedirectCall::class to RedirectMethodSerializer,
    )

    fun KSFunctionDeclaration.getInjection(): Pair<AnnotationSpec, InjectionSerializer>? {
        val annotations = this.annotations.associateBy { it.annotationType.resolve().declaration.qualifiedName!!.asString() }
        for ((klass, serializer) in serializers) {
            val annotation = annotations[klass.qualifiedName] ?: continue
            return serializer.readAnnotation(annotation) to serializer
        }
        return null
    }

    fun KSClassDeclaration.getMixinAnnotation(): AnnotationSpec {
        val annotation = this.annotations.first {
            it.annotationType.resolve().declaration.qualifiedName!!.asString() == KMixin::class.qualifiedName
        }
        return with(annotation) {
            AnnotationSpec.builder(MIXIN_CLASS)
                .addMember("value", "\$T.class", getAsClass("value").toJava())
                .addMember("priority", "\$L", getAsInt("priority"))
                .addMember("remap", "\$L", getAsBoolean("remap"))
                .build()
        }
    }
}
