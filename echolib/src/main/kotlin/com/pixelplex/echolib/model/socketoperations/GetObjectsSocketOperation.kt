package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.model.GrapheneObject
import com.pixelplex.echolib.support.Api
import com.pixelplex.echolib.support.getId

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
    val api: Api,
    val ids: Array<String>,
    method: SocketMethodType = SocketMethodType.CALL,
    callId: Int,
    callback: Callback<List<GrapheneObject>>

) : SocketOperation<List<GrapheneObject>>(
    method,
    callId,
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

            add(JsonArray().apply { identifiersJson })
        }

    override val apiId: Int
        get() = api.getId()
}
