package at.hannibal2.skyhanni.kmixin

import at.hannibal2.skyhanni.kmixin.annotations.InjectionMapping.getInjection
import at.hannibal2.skyhanni.kmixin.annotations.InjectionMapping.getMixinAnnotation
import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import java.io.OutputStreamWriter
import javax.lang.model.element.Modifier

class KMixinProcessor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {

        val symbols = resolver.getSymbolsWithAnnotation(KMixin::class.qualifiedName!!).toList()
        val validSymbols = symbols.mapNotNull { validateSymbol(it) }

        if (validSymbols.isNotEmpty()) {
            generateFile(validSymbols, logger)
        }

        return emptyList()
    }

    private fun validateSymbol(symbol: KSAnnotated): KSClassDeclaration? {
        if (!symbol.validate()) {
            logger.warn("Symbol is not valid: $symbol")
            return null
        }

        if (symbol !is KSClassDeclaration) {
            logger.error("@KMixin is only valid on class declarations", symbol)
            return null
        }

        if (symbol.classKind != ClassKind.OBJECT) {
            logger.error("@KMixin is only valid on kotlin objects", symbol)
            return null
        }

        return symbol
    }

    private fun generateFile(symbols: List<KSClassDeclaration>, logger: KSPLogger) {

        for (symbol in symbols) {

            val type = TypeSpec.classBuilder(symbol.simpleName.asString())
                .addAnnotation(symbol.getMixinAnnotation())
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)

            val fields = mutableListOf<FieldSpec>()
            val methods = mutableListOf<MethodSpec>()

            symbol.getDeclaredFunctions().forEach { function ->
                val injector = function.getInjection() ?: return@forEach
                require(function.isPublic()) { "Mixin functions must be public" }

                injector.second.write(
                    symbol, function,
                    { method -> methods.add(method.addAnnotation(injector.first).build()) },
                    { field -> fields.add(field.build()) }
                )
            }

            fields.distinct().forEach { type.addField(it) }
            methods.distinct().forEach { type.addMethod(it) }

            val file = codeGenerator.createNewFile(
                Dependencies(true, symbol.containingFile!!),
                "at.hannibal2.skyhanni.mixins.transformers.generated",
                symbol.simpleName.asString(),
                "java"
            )

            OutputStreamWriter(file).use {
                JavaFile.builder(
                    "at.hannibal2.skyhanni.mixins.transformers.generated",
                    type.build()
                ).build().writeTo(it)
            }
        }
    }
}
