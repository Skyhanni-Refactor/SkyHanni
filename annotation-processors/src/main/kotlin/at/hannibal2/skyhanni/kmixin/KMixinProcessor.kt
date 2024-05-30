package at.hannibal2.skyhanni.kmixin

import at.hannibal2.skyhanni.kmixin.ProcessorUtils.asJavaString
import at.hannibal2.skyhanni.kmixin.injectors.InjectSerializer
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import java.io.OutputStreamWriter

private val injectors = mapOf(
    "Inject" to InjectSerializer
)

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

            val mixin = symbol.findAnnotation("KMixin")!!
                    .arguments.find { it.name?.asString() == "value" }!!
                    .value as KSAnnotation

            OutputStreamWriter(file).use {
                it.write("package at.hannibal2.skyhanni.mixins.transformers.generated;\n\n")
                it.write("import org.spongepowered.asm.mixin.Mixin;\n\n")
                it.write("import org.spongepowered.asm.mixin.injection.*;\n\n")
                it.write("${mixin.asJavaString()}\n")
                it.write("class ${symbol.simpleName.asString()} {\n")

                symbol.getAllFunctions().forEach { function ->
                    it.write("\n")
                    for ((annotation, serializer) in injectors) {
                        function.findAnnotation(annotation)?.let { a ->
                            serializer.write(symbol, function, a, it)
                        }
                    }
                }

                it.write("\n}\n")
            }
        }
    }

    private fun KSAnnotated.findAnnotation(name: String): KSAnnotation? {
        return annotations.find { it.shortName.asString() == name }
    }
}
