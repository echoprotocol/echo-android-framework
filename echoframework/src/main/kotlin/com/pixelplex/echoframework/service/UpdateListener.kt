package com.pixelplex.echoframework.service

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
