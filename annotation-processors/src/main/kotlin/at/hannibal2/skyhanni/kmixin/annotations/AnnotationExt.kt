package at.hannibal2.skyhanni.kmixin.annotations

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSType
import kotlin.reflect.KClass

private fun KSAnnotation.get(id: String): Any? {
    return this.arguments.first { it.name?.asString() == id }.value
}

fun KSAnnotation.getAsString(id: String): String {
    return this.get(id) as String
}

fun KSAnnotation.getAsBoolean(id: String): Boolean {
    return this.get(id) as Boolean
}

fun KSAnnotation.getAsInt(id: String): Int {
    return this.get(id) as Int
}

fun KSAnnotation.getAsInjectionKind(id: String): InjectionKind {
    val type = this.get(id) as KSType
    return InjectionKind.valueOf(type.declaration.simpleName.asString())
}

fun KSAnnotation.getAsTargetLocation(id: String): TargetShift {
    val type = this.get(id) as KSType
    return TargetShift.valueOf(type.declaration.simpleName.asString())
}

fun <T : Enum<T>> KSAnnotation.getAsEnum(id: String, klass: KClass<T>): T {
    val type = this.get(id) as KSType
    return java.lang.Enum.valueOf(klass.java, type.declaration.simpleName.asString())
}

fun KSAnnotation.getAsClass(id: String): KSType {
    return this.get(id) as KSType
}


