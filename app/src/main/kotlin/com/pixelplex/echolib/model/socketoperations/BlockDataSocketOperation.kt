package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.pixelplex.echolib.model.DynamicGlobalProperties

/**
 * Returns the block chain’s rapidly-changing properties. The returned object contains information
 * that changes every block interval such as the head block number, the next witness, etc.
 *
 * @return [DynamicGlobalProperties]
 *
 * @author Daria Pechkovskaya
 */
class BlockDataSocketOperation(
    method: SocketMethodType = SocketMethodType.CALL,
    callId: Int,
    apiId: Int,
    result: OperationResult<DynamicGlobalProperties>
) : SocketOperation(method, callId, apiId, result) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.BLOCK_DATA.key)
            add(JsonArray())
        }
}
