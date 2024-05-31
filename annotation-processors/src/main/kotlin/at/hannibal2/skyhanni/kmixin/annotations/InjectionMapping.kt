package at.hannibal2.skyhanni.kmixin.annotations

import at.hannibal2.skyhanni.kmixin.injectors.InjectSerializer
import at.hannibal2.skyhanni.kmixin.injectors.InjectionSerializer
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import kotlin.reflect.KClass

object InjectionMapping {

    private val injections = mapOf<KClass<*>, KSAnnotation.() -> String>(
        KInject::class to {
            construct("Inject") {
                add("method = \"${getAsString("method")}\"")
                add("at = @At(value = \"${getAsInjectionKind("kind").name}\")")
                if (getAsBoolean("cancellable")) {
                    add("cancellable = true")
                }
                if (getAsBoolean("captureLocals"))  {
                    add("locals = org.spongepowered.asm.mixin.injection.callback.LocalCapture.CAPTURE_FAILHARD")
                }
            }
        }
    )

    private val serializers = mapOf<KClass<*>, InjectionSerializer>(
        KInject::class to InjectSerializer
    )

    fun KSFunctionDeclaration.getInjection(): Pair<String, InjectionSerializer>? {
        val annotations = this.annotations.associateBy { it.annotationType.resolve().declaration.qualifiedName!!.asString() }
        for ((klass, mapper) in injections) {
            val annotation = annotations[klass.qualifiedName] ?: continue
            return annotation.mapper() to serializers[klass]!!
        }
        return null
    }

    fun KSClassDeclaration.getMixin(): String {
        val annotation = this.annotations.first {
            it.annotationType.resolve().declaration.qualifiedName!!.asString() == KMixin::class.qualifiedName
        }
        return with(annotation) {
            val value = getAsClass("value")
            val priority = getAsInt("priority")
            val remap = getAsBoolean("remap")

            construct("Mixin") {
                add("value = ${value.declaration.qualifiedName!!.asString()}.class")
                add("priority = $priority")
                add("remap = $remap")
            }
        }
    }

    private fun construct(annotation: String, handle: MutableList<String>.() -> Unit): String {
        var result = "@$annotation("
        val args = mutableListOf<String>()
        args.handle()
        result += args.joinToString(", ")
        result += ")"
        return result
    }
}
