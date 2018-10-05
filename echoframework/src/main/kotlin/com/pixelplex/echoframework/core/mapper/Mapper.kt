package com.pixelplex.echoframework.core.mapper

/**
 * Encapsulates logic, associated with data mapping
 *
 * @author Daria Pechkovskaya
 */
interface Mapper<T> {

    /**
     * Maps [String] data to typed object
     */
    fun map(data: String): T?

}
