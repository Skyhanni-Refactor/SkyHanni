package at.hannibal2.skyhanni.kmixin.injectors

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import java.io.OutputStreamWriter

interface InjectorSerializer {

    fun write(klass: KSClassDeclaration, function: KSFunctionDeclaration, annotation: KSAnnotation, writer: OutputStreamWriter)
}