package at.hannibal2.skyhanni.kmixin.injectors

import at.hannibal2.skyhanni.kmixin.annotations.KSelf
import at.hannibal2.skyhanni.kmixin.annotations.KShadow
import at.hannibal2.skyhanni.kmixin.annotations.SHADOW_CLASS
import at.hannibal2.skyhanni.kmixin.annotations.getAsBoolean
import at.hannibal2.skyhanni.kmixin.hasAnnotation
import at.hannibal2.skyhanni.kmixin.toJava
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.javapoet.FieldSpec
import javax.lang.model.element.Modifier
import kotlin.reflect.KClass

object InjectionUtils {

    fun KSAnnotated.getAnnotation(annotation: KClass<out Annotation>): KSAnnotation {
        return this.annotations.first {
            it.annotationType.resolve().declaration.qualifiedName!!.asString() == annotation.qualifiedName
        }
    }

    fun gatherShadows(function: KSFunctionDeclaration, fieldWriter: (FieldSpec.Builder) -> Unit) {
        function.parameters
            .filter { it.hasAnnotation(KShadow::class) }
            .forEach {
                val annotation = it.getAnnotation(KShadow::class)

                val spec = FieldSpec.builder(it.type.toJava(), it.name!!.asString())
                    .addModifiers(Modifier.PRIVATE)
                    .addAnnotation(SHADOW_CLASS)

                if (annotation.getAsBoolean("final")) {
                    spec.addModifiers(Modifier.FINAL)
                }

                fieldWriter(
                    FieldSpec.builder(it.type.toJava(), it.name!!.asString())
                        .addModifiers(Modifier.PRIVATE)
                        .addAnnotation(SHADOW_CLASS)
                )
            }
    }

    fun createParameterList(function: KSFunctionDeclaration): String {
        return function.parameters.joinToString(", ") {
            when {
                it.hasAnnotation(KSelf::class) -> "(${it.type.toJava()}) (Object) this"
                it.hasAnnotation(KShadow::class) -> it.name!!.asString()
                else -> it.name!!.asString()
            }
        }
    }
}
