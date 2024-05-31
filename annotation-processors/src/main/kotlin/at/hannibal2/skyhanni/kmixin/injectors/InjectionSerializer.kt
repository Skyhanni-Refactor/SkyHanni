package at.hannibal2.skyhanni.kmixin.injectors

import at.hannibal2.skyhanni.kmixin.annotations.KStatic
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter

interface InjectionSerializer {

    fun write(
        klass: KSClassDeclaration, function: KSFunctionDeclaration,
        funWriter: (String) -> Unit, shadowWriter: (KSValueParameter) -> Unit
    )

    @OptIn(KspExperimental::class)
    companion object {

        internal fun getModifiers(function: KSFunctionDeclaration): String {
            return if (function.isAnnotationPresent(KStatic::class)) "private static" else "private"
        }
    }
}
