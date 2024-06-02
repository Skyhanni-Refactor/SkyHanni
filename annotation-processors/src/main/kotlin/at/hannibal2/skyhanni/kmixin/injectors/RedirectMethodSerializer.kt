package at.hannibal2.skyhanni.kmixin.injectors

import at.hannibal2.skyhanni.kmixin.addAnnotation
import at.hannibal2.skyhanni.kmixin.addModifiers
import at.hannibal2.skyhanni.kmixin.addParameter
import at.hannibal2.skyhanni.kmixin.annotations.AT_CLASS
import at.hannibal2.skyhanni.kmixin.annotations.KShadow
import at.hannibal2.skyhanni.kmixin.annotations.KStatic
import at.hannibal2.skyhanni.kmixin.annotations.REDIRECT_CLASS
import at.hannibal2.skyhanni.kmixin.annotations.getAsBoolean
import at.hannibal2.skyhanni.kmixin.annotations.getAsInt
import at.hannibal2.skyhanni.kmixin.annotations.getAsString
import at.hannibal2.skyhanni.kmixin.hasAnnotation
import at.hannibal2.skyhanni.kmixin.toJava
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Modifier

object RedirectMethodSerializer : InjectionSerializer {

    override fun readAnnotation(function: KSFunctionDeclaration, annotation: KSAnnotation): AnnotationSpec = with(annotation) {
        AnnotationSpec.builder(REDIRECT_CLASS)
            .addMember("method", "\$S", getAsString("method"))
            .addAnnotation("at", AT_CLASS) {
                add("value", "\$S", "INVOKE")
                add("target", "\$S", getAsString("target"))
                add("ordinal", "\$L", getAsInt("ordinal"))
            }
            .addMember("remap", "\$L", getAsBoolean("remap"))
            .build()
    }

    override fun write(
        klass: KSClassDeclaration,
        annotation: AnnotationSpec,
        function: KSFunctionDeclaration,
        methodWriter: (MethodSpec.Builder) -> Unit,
        fieldWriter: (FieldSpec.Builder) -> Unit
    ) {
        val returnType = function.returnType!!.toJava()
        val method = MethodSpec.methodBuilder(function.simpleName.asString())
            .addModifiers(Modifier.PRIVATE)
            .addModifiers(function.hasAnnotation(KStatic::class), Modifier.STATIC)
            .addAnnotation(annotation)
            .returns(returnType)

        function.parameters
            .filter { !it.hasAnnotation(KShadow::class) }
            .forEach {
                require(!it.hasDefault) { "Default parameters are not supported" }
                method.addParameter(it)
            }

        if (returnType == TypeName.VOID) {
            method.addStatement("\$T.INSTANCE.${function.simpleName.asString()}(${InjectionUtils.createParameterList(function)})", klass.toJava())
        } else {
            method.addStatement("return \$T.INSTANCE.${function.simpleName.asString()}(${InjectionUtils.createParameterList(function)})", klass.toJava())
        }

        methodWriter(method)
        InjectionUtils.gatherShadows(function, fieldWriter, methodWriter)
    }

}
