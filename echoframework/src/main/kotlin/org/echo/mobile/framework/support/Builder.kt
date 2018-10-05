package org.echo.mobile.framework.support

/**
 * Base template for all factory classes.
 *
 * @author Daria Pechkovskaya
 */
internal interface Builder<T> {

    /**
     * Must be implemented and return the factory is supposed to build.
     *
     * @return: A usable instance of a given class.
     */
    fun build(): T
}
