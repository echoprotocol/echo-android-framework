package com.pixelplex.echoframework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.pixelplex.echoframework.Callback

/**
 * Stop receiving any notifications.
 * This unsubscribes from all subscribed markets and objects.
 *
 * @author Daria Pechkovskaya
 */
class CancelAllSubscriptionsSocketOperation(
    override val apiId: Int,
    callId: Int,
    callback: Callback<Any>
) : SocketOperation<Any>(SocketMethodType.CALL, callId, Any::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.CANCEL_ALL_SUBSCRIPTIONS.key)
            add(JsonArray())
        }

    override fun fromJson(json: String): Any? {
        val jsonObject = JsonParser().parse(json).asJsonObject
        return if (jsonObject.has(RESULT_KEY)) Any() else null
    }
}