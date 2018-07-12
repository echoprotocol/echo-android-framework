package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement

/**
 * Get the chain id.
 *
 * @return Chain id
 *
 * @author Daria Pechkovskaya
 */
class GetChainIdSocketOperation(
    method: SocketMethodType = SocketMethodType.CALL,
    callId: Int,
    apiId: Int,
    result: OperationResult<String>
) : SocketOperation(method, callId, apiId, result) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.CHAIN_ID.key)
            add(JsonArray())
        }

}
