package com.pixelplex.echoframework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.core.mapper.ObjectMapper
import com.pixelplex.echoframework.model.GrapheneObject

/**
 * Get the objects corresponding to the provided IDs.
 * If any of the provided IDs does not map to an object, a null variant is returned in its position.
 *
 * @param ids of the objects to retrieve
 * @param mapper Mapper for received object
 * @return The objects retrieved, in the order they are mentioned in ids
 *
 * @author Daria Pechkovskaya
 */
class GetObjectsSocketOperation<T : GrapheneObject>(
    override val apiId: Int,
    val ids: Array<String>,
    val mapper: ObjectMapper<T>,
    callId: Int,
    callback: Callback<List<T>>

) : SocketOperation<List<T>>(
    SocketMethodType.CALL,
    callId,
    listOf<T>().javaClass,
    callback
) {

    @Suppress("UNUSED_EXPRESSION")
    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.OBJECTS.key)

            val identifiersJson = JsonArray()
            ids.forEach { item -> identifiersJson.add(item) }

            add(JsonArray().apply { add(identifiersJson) })
        }

    override fun fromJson(json: String): List<T> {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        val result = jsonTree.asJsonObject.get(RESULT_KEY)?.asJsonArray
            ?: return emptyList()

        return result.mapNotNull { jsonElement ->
            mapper.map(jsonElement.toString())
        }
    }

    companion object {
        private const val RESULT_KEY = "result"
    }

}
