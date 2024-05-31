package at.hannibal2.skyhanni.kmixin

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter

object ProcessorUtils {

    private val typeToJava = mapOf(
        "kotlin.Unit" to "void",
        "kotlin.Unit?" to "Void",
        "kotlin.Boolean" to "boolean",
        "kotlin.Boolean?" to "Boolean",
        "kotlin.Byte" to "byte",
        "kotlin.Byte?" to "Byte",
        "kotlin.Short" to "short",
        "kotlin.Short?" to "Short",
        "kotlin.Int" to "int",
        "kotlin.Int?" to "Integer",
        "kotlin.Long" to "long",
        "kotlin.Long?" to "Long",
        "kotlin.Float" to "float",
        "kotlin.Float?" to "Float",
        "kotlin.Double" to "double",
        "kotlin.Double?" to "Double",
        "kotlin.Char" to "char",
        "kotlin.Char?" to "Character",
        "kotlin.String" to "String"
    )

    fun KSType.toJavaString(): String {
        val name = this.declaration.qualifiedName!!.asString()
        if (this.isMarkedNullable && typeToJava["$name?"] != null) {
            return typeToJava["$name?"]!!
        }
        return typeToJava[name] ?: name
    }

    fun KSFunctionDeclaration.asJavaMethodString(filter: (KSValueParameter) -> Boolean): String {
        val params = this.parameters
            .filter(filter)
            .joinToString(", ") {
                "${it.type.resolve().toJavaString()} ${it.name!!.asString()}"
            }
        return "${this.simpleName.asString()}($params)"
    }

    fun KSFunctionDeclaration.asJavaCallString(): String {
        val qualified = this.qualifiedName!!
        return "${qualified.getQualifier()}.INSTANCE.${qualified.getShortName()}(${this.parameters.joinToString(", ") {
            it.name!!.asString()
        }});"
    }
}
