package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.model.Block
import com.pixelplex.echolib.support.model.Api
import com.pixelplex.echolib.support.model.getId

/**
 * Retrieve a full, signed block.
 *
 * @param blockNumber Height of the block whose header should be returned
 * @return the referenced [Block] object, or null if no matching block was found
 *
 * @author Daria Pechkovskaya
 */
class GetBlockSocketOperation(
    val api: Api,
    val blockNumber: String,
    method: SocketMethodType = SocketMethodType.CALL,
    callId: Int,
    callback: Callback<Block>

) : SocketOperation<Block>(method, callId, Block::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.BLOCK.key)
            add(JsonArray().apply { blockNumber })
        }

    override val apiId: Int
        get() = api.getId()
}
