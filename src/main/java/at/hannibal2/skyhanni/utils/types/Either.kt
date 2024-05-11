package at.hannibal2.skyhanni.utils.types

sealed class Either<out L, out R> {

    data class Left<L>(val value: L) : Either<L, Nothing>()
    data class Right<R>(val value: R) : Either<Nothing, R>()

    val isLeft get() = this is Left<L>
    val isRight get() = this is Right<R>

    fun ifLeft(block: (L) -> Unit): Either<L, R> {
        if (this is Left<L>) block(value)
        return this
    }

    fun ifRight(block: (R) -> Unit): Either<L, R> {
        if (this is Right<R>) block(value)
        return this
    }

    fun run(left: (L) -> Unit, right: (R) -> Unit): Either<L, R> {
        if (this is Left<L>) left(value)
        if (this is Right<R>) right(value)
        return this
    }
}
