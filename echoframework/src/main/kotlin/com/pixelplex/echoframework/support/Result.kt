package com.pixelplex.echoframework.support

/**
 * Encapsulates operation results
 *
 * <p>
 *     Provides abstraction over success\failure operation results
 * </p>
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
fun <V> toValue(value: V): Result<Nothing, V> = Result.Value(value)

/**
 * Converts [Result] to [Result.Error]
 */
fun <E : Exception> toError(value: E): Result<E, Nothing> = Result.Error(value)

/**
 * Maps value to another type of value by function
 * @param f Function to map
 */
inline infix fun <E : Exception, V, V2> Result<E, V>.map(f: (V) -> V2): Result<E, V2> =
    when (this) {
        is Result.Error -> this
        is Result.Value -> Result.Value(f(this.value))
    }

/**
 * Breaks result on error\value and provides functional way to work with them
 */
inline fun <E : Exception, V, A> Result<E, V>.fold(v: (V) -> A, e: (E) -> A): A = when (this) {
    is Result.Error -> e(this.error)
    is Result.Value -> v(this.value)
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
