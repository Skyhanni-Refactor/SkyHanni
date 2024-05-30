package at.hannibal2.skyhanni.kmixin.injectors

import at.hannibal2.skyhanni.kmixin.ProcessorUtils.asJavaCallString
import at.hannibal2.skyhanni.kmixin.ProcessorUtils.asJavaMethodString
import at.hannibal2.skyhanni.kmixin.ProcessorUtils.asJavaObjectReferenceString
import at.hannibal2.skyhanni.kmixin.ProcessorUtils.asJavaString
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import java.io.OutputStreamWriter

object InjectSerializer : InjectorSerializer {

    override fun write(klass: KSClassDeclaration, function: KSFunctionDeclaration, annotation: KSAnnotation, writer: OutputStreamWriter) {
        writer.write("    ${annotation.asJavaString()}\n")
        writer.write("    private static void ${function.asJavaMethodString()} {\n")
        writer.write("        ${function.asJavaCallString()}\n")
        writer.write("    }\n")
    }
}