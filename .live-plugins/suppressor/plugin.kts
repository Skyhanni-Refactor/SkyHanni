import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import liveplugin.registerInspection
import org.jetbrains.kotlin.idea.base.utils.fqname.fqName
import org.jetbrains.kotlin.idea.codeinsight.api.classic.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.idea.util.AnnotationModificationHelper
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtVisitorVoid
import org.jetbrains.kotlin.types.typeUtil.supertypes

// depends-on-plugin org.jetbrains.kotlin

val skyhanniEvent = "at.hannibal2.skyhanni.api.event.SkyHanniEvent"
val handleEvent = "HandleEvent"

registerInspection(HandleEventInspectionKotlin())

class HandleEventInspectionKotlin : AbstractKotlinInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {

        val visitor = object : KtVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                if (function.valueParameters.size == 1) {
                    val parameter = function.valueParameters[0]
                    val supers = parameter.type()?.supertypes() ?: return
                    val hasEventAnnotation = function.annotationEntries.any { it.shortName!!.asString() == handleEvent }
                    if (!supers.any { it.fqName?.asString() == skyhanniEvent }) {
                        if (hasEventAnnotation) {
                            holder.registerProblem(
                                function,
                                "Function should not be annotated with @HandleEvent if it does not take a SkyHanniEvent"
                            )
                        } else {
                            return
                        }
                    }
                    if (hasEventAnnotation) return
                    holder.registerProblem(
                        function,
                        "Event handler function should be annotated with @HandleEvent",
                        HandleEventQuickFix()
                    )
                }
            }
        }

        return visitor
    }
    override fun getDisplayName() = "Replace \"hello\" with \"Hello world\" in Kotlin"
    override fun getShortName() = "HelloWorldInspectionKotlin"
    override fun getGroupDisplayName() = "Live plugin"
    override fun isEnabledByDefault() = true
}

class HandleEventQuickFix : LocalQuickFix {
    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val function = descriptor.psiElement as KtNamedFunction
        AnnotationModificationHelper.addAnnotation(
            function,
            FqName("at.hannibal2.skyhanni.api.event.HandleEvent"),
            null,
            null,
            { null },
                " ",
            null
        )
    }

    override fun getName() = "Annotate with @HandleEvent"

    override fun getFamilyName() = name
}
