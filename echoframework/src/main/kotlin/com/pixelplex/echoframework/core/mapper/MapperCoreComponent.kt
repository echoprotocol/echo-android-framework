package com.pixelplex.echoframework.core.mapper

/**
 * Parsed objects from json
 *
 * @author Daria Pechkovskaya
 */
interface MapperCoreComponent {

    /**
     * Parse object by type
     * @param json Json string to parse
     * @param type Class type for parse
     * @return Parsed class
     */
    fun <T> map(json: String, type: Class<T>): T?

}
