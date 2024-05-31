package at.hannibal2.skyhanni.kmixin.injectors

import at.hannibal2.skyhanni.kmixin.addParameter
import at.hannibal2.skyhanni.kmixin.annotations.KShadow
import at.hannibal2.skyhanni.kmixin.annotations.KStatic
import at.hannibal2.skyhanni.kmixin.toJava
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.javapoet.MethodSpec
import javax.lang.model.element.Modifier

object InjectSerializer : InjectionSerializer {

    @OptIn(KspExperimental::class)
    override fun write(
        klass: KSClassDeclaration,
        function: KSFunctionDeclaration,
        funWriter: (String) -> Unit,
        shadowWriter: (KSValueParameter) -> Unit
    ) {
        val method = MethodSpec.methodBuilder(function.simpleName.asString())
            .addModifiers(Modifier.PRIVATE)
            .returns(Void.TYPE)

        if (function.isAnnotationPresent(KStatic::class)) {
            method.addModifiers(Modifier.STATIC)
        }

        function.parameters
            .filter { !it.isAnnotationPresent(KShadow::class) }
            .forEach {
                require(!it.hasDefault) { "Default parameters are not supported" }
                method.addParameter(it)
            }

        method.addStatement(
            "\$T.INSTANCE.${function.simpleName.asString()}(${function.parameters.joinToString(", ") {
                it.name!!.asString()
            }});",
            klass.toJava()
        )

        funWriter(method.build().toString())
    }
}
