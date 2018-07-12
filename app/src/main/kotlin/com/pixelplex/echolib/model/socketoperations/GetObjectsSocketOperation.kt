package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement

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
    method: SocketMethodType = SocketMethodType.CALL,
    callId: Int,
    apiId: Int,
    result: OperationResult<*>,
    val ids: Array<String>
) : SocketOperation(method, callId, apiId, result) {

    @Suppress("UNUSED_EXPRESSION")
    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.OBJECTS.key)

            val identifiersJson = JsonArray()
            ids.forEach { item -> identifiersJson.add(item) }

            add(JsonArray().apply { identifiersJson })
        }

}
