package com.pixelplex.echoframework.support

/**
 * Encapsulates operation results
 *
 * Provides abstraction over success\failure operation results
 *
 * @author Dmitriy Bushuev
 */
sealed class Result<out E : Exception, out V> {
    /**
     * Error operation result
     */
    data class Error<out E : Exception>(val error: E) : Result<E, Nothing>()

    /**
     * Value operation result
     */
    data class Value<out V>(val value: V) : Result<Nothing, V>()
}

/**
 * Converts [Result] to [Result.Value]
 */
fun <V> V.toValue(): Result<Nothing, V> = Result.Value(this)

/**
 * Converts [Result] to [Result.Error]
 */
fun <E : Exception> E.toError(): Result<E, Nothing> = Result.Error(this)

/**
 * Maps value to another type of value by function
 *
 * Returns [Result.Value<[V2]>] with applied [f] to current value.
 * [Result.Error<[E], [V2]>] if [f] will throw an exception
 *
 * @param f Function to map
 */
inline infix fun <E : Exception, V, V2> Result<E, V>.map(f: (V) -> V2): Result<E, V2> =
    when (this) {
        is Result.Error -> this
        is Result.Value -> try {
            Result.Value(f(this.value))
        } catch (e: Exception) {
            Result.Error(e as E)
        }
    }

/**
 * Breaks result on error\value and provides functional way to work with them
 */
inline fun <E : Exception, V, A> Result<E, V>.fold(v: (V) -> A, e: (E) -> A): A = when (this) {
    is Result.Error -> e(this.error)
    is Result.Value -> v(this.value)
}

/**
 * Apply function over result success value
 */
inline fun <E : Exception, V> Result<E, V>.value(v: (V) -> Unit): Result<E, V> = when (this) {
    is Result.Error -> this
    is Result.Value -> {
        v(this.value)
        this
    }
}

/**
 * Apply function over result error value
 */
inline fun <E : Exception, V> Result<E, V>.error(e: (E) -> Unit): Result<E, V> = when (this) {
    is Result.Error -> {
        e(this.error)
        this
    }
    is Result.Value -> this
}

/**
 * Maps error to another type of error by function
 * @param f Function to map
 */
inline infix fun <E : Exception, E2 : Exception, V> Result<E, V>.mapError(f: (E) -> E2): Result<E2, V> =
    when (this) {
        is Result.Error -> Result.Error(f(error))
        is Result.Value -> this
    }

/**
 * Maps [Result] to another type of [Result]
 * @param f Function to map
 */
inline infix fun <E : Exception, V, V2> Result<E, V>.flatMap(f: (V) -> Result<E, V2>): Result<E, V2> =
    when (this) {
        is Result.Error -> this
        is Result.Value -> f(value)
    }

/**
 * Apply new [Result] type
 * @param f new [Result]
 */
infix fun <E : Exception, V, V2> Result<E, (V) -> V2>.apply(f: Result<E, V>): Result<E, V2> =
    when (this) {
        is Result.Error -> this
        is Result.Value -> f.map(this.value)
    }

/**
 * Unwraps success result value or throws and error
 */
fun <E : Exception, V> Result<E, V>.dematerialize(): V =
    when (this) {
        is Result.Error -> throw error
        is Result.Value -> value
    }
