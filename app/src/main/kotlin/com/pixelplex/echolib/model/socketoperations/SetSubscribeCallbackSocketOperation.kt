package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement

/**
 * Register global subscription callback to object.
 * Every notification initiated by the full node will carry a particular id as defined by the
 * user with the identifier parameter.
 *
 * @param needClearFilter Clearing subscription filter
 * @return Notice of changes
 *
 * @author Daria Pechkovskaya
 */
class SetSubscribeCallbackSocketOperation(
    method: SocketMethodType = SocketMethodType.CALL,
    callId: Int,
    apiId: Int,
    result: OperationResult<*>,
    val needClearFilter: Boolean
) : SocketOperation(method, callId, apiId, result) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.SUBSCRIBE_CALLBACK.key)
            add(JsonArray().apply {
                add(callId)
                add(needClearFilter)
            })
        }

}
