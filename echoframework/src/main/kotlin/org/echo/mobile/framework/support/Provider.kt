package org.echo.mobile.framework.support

/**
 * Encapsulate simple provide logic
 *
 * @author Daria Pechkovskaya
 */
interface Provider<T> {

    /**
     * Provides typed data
     */
    fun provide(): T
}