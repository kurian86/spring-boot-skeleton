package es.bdo.skeleton.shared.cqrs

fun interface CommandHandler<T, R> {
    fun handle(command: T): Result<R>
}
