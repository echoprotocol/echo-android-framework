package org.echo.mobile.framework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback

/**
 * Stop receiving any notifications.
 * This unsubscribes from all subscribed markets and objects.
 *
 * @author Daria Pechkovskaya
 */
class CancelAllSubscriptionsSocketOperation(
    override val apiId: Int,
    callId: Int,
    callback: Callback<Boolean>
) : SocketOperation<Boolean>(SocketMethodType.CALL, callId, Boolean::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.CANCEL_ALL_SUBSCRIPTIONS.key)
            add(JsonArray())
        }

    override fun fromJson(json: String): Boolean {
        val jsonObject = JsonParser().parse(json).asJsonObject
        return jsonObject.has(RESULT_KEY)
    }
}
