@file:OptIn(KotlinPoetJavaPoetPreview::class)

package at.hannibal2.skyhanni.kmixin

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.kotlinpoet.javapoet.KotlinPoetJavaPoetPreview
import com.squareup.kotlinpoet.javapoet.toJClassName
import com.squareup.kotlinpoet.javapoet.toJTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

fun KSType.toJava(): TypeName {
    return this.toTypeName().toJTypeName()
}

fun KSTypeReference.toJava(): TypeName {
    return this.resolve().toJava()
}

fun KSClassDeclaration.toJava(): TypeName {
    return this.toClassName().toJClassName()
}

fun MethodSpec.Builder.addParameter(parameter: KSValueParameter) {
    this.addParameter(parameter.type.toJava(), parameter.name!!.asString())
}
