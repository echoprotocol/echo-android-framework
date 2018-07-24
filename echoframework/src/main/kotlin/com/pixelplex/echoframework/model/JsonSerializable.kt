package com.pixelplex.echoframework.model

import com.google.gson.JsonElement

import java.io.Serializable

/**
 * Describes functionality of entities for which it is required
 * to have specific json string representation
 *
 * @author Dmitriy Bushuev
 */
interface JsonSerializable : Serializable {

    /**
     * Converts entity to json string representation
     *
     * @return Json string representation
     */
    fun toJsonString(): String?

    /**
     * Converts entity to json object representation
     *
     * @return JsonObject representation
     */
    fun toJsonObject(): JsonElement?

}
