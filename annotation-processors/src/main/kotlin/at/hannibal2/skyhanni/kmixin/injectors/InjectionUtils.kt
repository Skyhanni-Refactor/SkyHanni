package at.hannibal2.skyhanni.kmixin.injectors

import at.hannibal2.skyhanni.kmixin.annotations.KSelf
import at.hannibal2.skyhanni.kmixin.annotations.KShadow
import at.hannibal2.skyhanni.kmixin.annotations.SHADOW_CLASS
import at.hannibal2.skyhanni.kmixin.hasAnnotation
import at.hannibal2.skyhanni.kmixin.toJava
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.javapoet.FieldSpec
import javax.lang.model.element.Modifier

object InjectionUtils {

    fun gatherShadows(function: KSFunctionDeclaration, fieldWriter: (FieldSpec.Builder) -> Unit) {
        function.parameters
            .filter { it.hasAnnotation(KShadow::class) }
            .forEach {
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
