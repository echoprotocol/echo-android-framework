package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.support.model.Api
import com.pixelplex.echolib.support.model.getId

/**
 * Get the chain id.
 *
 * @return Chain id
 *
 * @author Daria Pechkovskaya
 */
class GetChainIdSocketOperation(
    val api: Api,
    method: SocketMethodType = SocketMethodType.CALL,
    callId: Int,
    callback: Callback<String>

) : SocketOperation<String>(method, callId, String::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.CHAIN_ID.key)
            add(JsonArray())
        }

    override val apiId: Int
        get() = api.getId()

}
