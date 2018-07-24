package com.pixelplex.echoframework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.ILLEGAL_ID
import com.pixelplex.echoframework.model.GrapheneObject

/**
 * Get the objects corresponding to the provided IDs.
 * If any of the provided IDs does not map to an object, a null variant is returned in its position.
 *
 * @param ids of the objects to retrieve
 * @return The objects retrieved, in the order they are mentioned in ids
 *
 * @author Daria Pechkovskaya
 */
class GetObjectsSocketOperation(
    override val apiId: Int,
    val ids: Array<String>,
    method: SocketMethodType = SocketMethodType.CALL,
    callback: Callback<List<GrapheneObject>>

) : SocketOperation<List<GrapheneObject>>(
    method,
    ILLEGAL_ID,
    listOf<GrapheneObject>().javaClass,
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

    override fun fromJson(json: String): List<GrapheneObject>? {
        return null
    }
}
