package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.support.model.Api
import com.pixelplex.echolib.support.model.getId

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
    val api: Api,
    val needClearFilter: Boolean,
    method: SocketMethodType = SocketMethodType.CALL,
    callId: Int,
    callback: Callback<Any>

) : SocketOperation<Any>(method, callId, Any::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.SUBSCRIBE_CALLBACK.key)
            add(JsonArray().apply {
                add(callId)
                add(needClearFilter)
            })
        }

    override val apiId: Int
        get() = api.getId()


}
