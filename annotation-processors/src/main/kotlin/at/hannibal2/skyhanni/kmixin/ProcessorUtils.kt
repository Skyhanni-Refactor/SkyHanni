package at.hannibal2.skyhanni.kmixin

import com.google.devtools.ksp.symbol.*

object ProcessorUtils {

    fun KSFunctionDeclaration.asJavaMethodString(): String {
        return "${this.simpleName.asString()}(${this.parameters.joinToString(", ") {
            "${it.type.resolve().declaration.qualifiedName!!.asString()} ${it.name!!.asString()}"
        }})"
    }

    fun KSFunctionDeclaration.asJavaCallString(): String {
        return "${this.qualifiedName!!.asString()}(${this.parameters.joinToString(", ") {
            it.name!!.asString()
        }});"
    }

    fun KSClassDeclaration.asJavaObjectReferenceString(): String {
        require(this.classKind == ClassKind.OBJECT)
        return "${this.qualifiedName!!.asString()}.INSTANCE"
    }

    private fun toJavaArgument(value: Any): String {
        return when (value) {
            is String -> "\"$value\""
            is KSAnnotation -> value.asJavaString()
            is KSType -> {
                if (value.declaration.modifiers.contains(Modifier.ENUM)) {
                    value.declaration.qualifiedName!!.asString()
                } else {
                    "${value.declaration.qualifiedName!!.asString()}.class"
                }
            }
            is Enum<*> -> "${value::class.simpleName}.${value.name}"
            is List<*> -> value.joinToString(", ", "{", "}") { toJavaArgument(it!!) }
            else -> value.toString()
        }
    }

    fun KSAnnotation.asJavaString(): String {
        val arguments = mutableListOf<String>()

        val args = this.arguments.associateBy { it.name?.asString() }
        val defaultArgs = this.defaultArguments.associateBy { it.name?.asString() }

        for ((name, value) in args) {
            if (defaultArgs[name] == value) continue
            val javaValue = toJavaArgument(value.value!!)
            arguments.add("$name = $javaValue")
        }

        return "$this(${arguments.joinToString(", ")})"
    }
}