package org.echo.mobile.framework.model

import java.io.Serializable

/**
 * Describes functionality of entities for which it is required
 * to have specific representation from json string
 *
 * @author Daria Pechkovskaya
 */
interface JsonDeserializable<T> : Serializable {

    /**
     * Converts json string to entity
     *
     * @param json Json string for parsing
     * @return Typed entity
     */
    fun fromJson(json: String): T?

}
