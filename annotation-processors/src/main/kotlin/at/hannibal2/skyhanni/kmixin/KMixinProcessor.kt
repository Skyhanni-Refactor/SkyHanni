package at.hannibal2.skyhanni.kmixin

import at.hannibal2.skyhanni.kmixin.ProcessorUtils.toJavaString
import at.hannibal2.skyhanni.kmixin.annotations.InjectionMapping.getInjection
import at.hannibal2.skyhanni.kmixin.annotations.InjectionMapping.getMixin
import at.hannibal2.skyhanni.kmixin.annotations.KMixin
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import java.io.OutputStreamWriter

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
            val file = codeGenerator.createNewFile(
                    Dependencies(true, symbol.containingFile!!),
                    "at.hannibal2.skyhanni.mixins.transformers.generated",
                    symbol.simpleName.asString(),
                    "java"
            )

            OutputStreamWriter(file).use {
                it.write("package at.hannibal2.skyhanni.mixins.transformers.generated;\n")
                it.write("\n")
                it.write("import org.spongepowered.asm.mixin.*;\n")
                it.write("import org.spongepowered.asm.mixin.injection.*;\n")
                it.write("\n")
                it.write("${symbol.getMixin()}\n")
                it.write("class ${symbol.simpleName.asString()} {\n")

                val functionDeclarations = mutableListOf<String>()
                val shadowDeclarations = mutableMapOf<String, KSType>()

                symbol.getDeclaredFunctions().forEach { function ->
                    functionDeclarations.add("\n")
                    val injector = function.getInjection() ?: return@forEach
                    functionDeclarations.add("    ${injector.first}\n")
                    injector.second.write(
                        symbol,
                        function,
                        { str -> functionDeclarations.add("    $str\n") },
                        { param -> shadowDeclarations[param.name!!.asString()] = param.type.resolve() }
                    )
                }

                shadowDeclarations.forEach { (name, type) ->
                    it.write("\n")
                    it.write("    @Shadow private ${type.toJavaString()} $name;\n")
                }

                functionDeclarations.forEach { str -> it.write(str) }

                it.write("}\n")
            }
        }
    }
}
