package org.echo.mobile.framework.service

/**
 * Encapsulates logic, associated with data updates
 *
 * @author Daria Pechkovskaya
 */
interface UpdateListener<T> {

    /**
     * Data updates event
     */
    fun onUpdate(data: T)
}
