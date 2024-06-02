package at.hannibal2.skyhanni.kmixin.injectors

import at.hannibal2.skyhanni.kmixin.annotations.KInject
import at.hannibal2.skyhanni.kmixin.annotations.KInjectAt
import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import at.hannibal2.skyhanni.kmixin.annotations.KPseudoMixin
import at.hannibal2.skyhanni.kmixin.annotations.KRedirectCall
import at.hannibal2.skyhanni.kmixin.annotations.KRedirectField
import at.hannibal2.skyhanni.kmixin.annotations.MIXIN_CLASS
import at.hannibal2.skyhanni.kmixin.annotations.PSEUDO_CLASS
import at.hannibal2.skyhanni.kmixin.annotations.getAsBoolean
import at.hannibal2.skyhanni.kmixin.annotations.getAsClass
import at.hannibal2.skyhanni.kmixin.annotations.getAsInt
import at.hannibal2.skyhanni.kmixin.annotations.getAsString
import at.hannibal2.skyhanni.kmixin.hasAnnotation
import at.hannibal2.skyhanni.kmixin.injectors.InjectionUtils.getAnnotation
import at.hannibal2.skyhanni.kmixin.injectors.inject.InjectAtSerializer
import at.hannibal2.skyhanni.kmixin.injectors.inject.InjectSerializer
import at.hannibal2.skyhanni.kmixin.injectors.redirect.RedirectFieldSerializer
import at.hannibal2.skyhanni.kmixin.injectors.redirect.RedirectMethodSerializer
import at.hannibal2.skyhanni.kmixin.toJava
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.TypeSpec
import kotlin.reflect.KClass

object Injectors {

    private val serializers = mapOf<KClass<*>, InjectionSerializer>(
        KInject::class to InjectSerializer,
        KInjectAt::class to InjectAtSerializer,
        KRedirectCall::class to RedirectMethodSerializer,
        KRedirectField::class to RedirectFieldSerializer,
    )

    fun KSFunctionDeclaration.getInjection(): Pair<AnnotationSpec, InjectionSerializer>? {
        val annotations =
            this.annotations.associateBy { it.annotationType.resolve().declaration.qualifiedName!!.asString() }
        for ((klass, serializer) in serializers) {
            val annotation = annotations[klass.qualifiedName] ?: continue
            return serializer.readAnnotation(this, annotation) to serializer
        }
        return null
    }

    fun TypeSpec.Builder.addMixinAnnotations(symbol: KSClassDeclaration): TypeSpec.Builder {
        if (symbol.hasAnnotation(KPseudoMixin::class)) {
            val annotation = symbol.getAnnotation(KPseudoMixin::class)
            addAnnotation(PSEUDO_CLASS)
            addAnnotation(with(annotation) {
                AnnotationSpec.builder(MIXIN_CLASS)
                    .addMember("targets", "\$S", getAsString("value"))
                    .addMember("priority", "\$L", getAsInt("priority"))
                    .addMember("remap", "\$L", getAsBoolean("remap"))
                    .build()
            })

        } else if (symbol.hasAnnotation(KMixin::class)) {
            val annotation = symbol.getAnnotation(KMixin::class)
            addAnnotation(with(annotation) {
                AnnotationSpec.builder(MIXIN_CLASS)
                    .addMember("value", "\$T.class", getAsClass("value").toJava())
                    .addMember("priority", "\$L", getAsInt("priority"))
                    .addMember("remap", "\$L", getAsBoolean("remap"))
                    .build()
            })
        } else {
            throw IllegalArgumentException("Class $symbol is not a mixin")
        }
        return this
    }
}
