package es.bdo.skeleton.shared.cqrs

fun interface QueryHandler<T, R> {
    fun handle(query: T): Result<R>
}
