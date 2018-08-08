package com.pixelplex.echoframework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.ILLEGAL_ID
import org.json.JSONObject

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
    override val apiId: Int,
    val needClearFilter: Boolean,
    callId: Int,
    callback: Callback<Any>
) : SocketOperation<Any>(SocketMethodType.CALL, callId, Any::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.SUBSCRIBE_CALLBACK.key)
            add(JsonArray().apply {
                add(callId)
                add(needClearFilter)
            })
        }

    override fun fromJson(json: String): Any? {
        val jsonObject = JsonParser().parse(json).asJsonObject
        return if (jsonObject.has(RESULT_KEY)) Any() else null
    }

    companion object {
        private const val RESULT_KEY = "result"
    }

}
