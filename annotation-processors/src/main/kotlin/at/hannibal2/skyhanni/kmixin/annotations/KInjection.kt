package at.hannibal2.skyhanni.kmixin.annotations

enum class InjectionKind {
    HEAD,
    TAIL,
    RETURN,
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class KInject(
    val method: String,
    val kind: InjectionKind,
    val cancellable: Boolean = false,
    val captureLocals: Boolean = false,
)
