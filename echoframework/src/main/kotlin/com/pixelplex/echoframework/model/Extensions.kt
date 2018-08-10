package com.pixelplex.echoframework.model

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import java.util.*

/**
 * Represents additional payload of models
 *
 * @author Dmitriy Bushuev
 */
class Extensions : GrapheneSerializable {

    private val extensions = ArrayList<JsonSerializable>()

    override fun toJsonString(): String? {
        return null
    }

    override fun toJsonObject(): JsonElement =
        JsonArray().apply {
            extensions.forEach { extension -> add(extension.toJsonObject()) }
        }

    override fun toBytes() = byteArrayOf(0)

    /**
     * Returns current extension list size
     */
    fun size() = extensions.size

    companion object {
        const val KEY_EXTENSIONS = "extensions"
    }

}
