package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.pixelplex.echolib.model.Block

/**
 * Retrieve a full, signed block.
 *
 * @param blockNumber Height of the block whose header should be returned
 * @return the referenced [Block] object, or null if no matching block was found
 *
 * @author Daria Pechkovskaya
 */
class GetBlockSocketOperation(
    method: SocketMethodType = SocketMethodType.CALL,
    callId: Int,
    apiId: Int,
    result: OperationResult<Block>,
    val blockNumber: String
) : SocketOperation(method, callId, apiId, result) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.BLOCK.key)
            add(JsonArray().apply { blockNumber })
        }

}
