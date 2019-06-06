package org.echo.mobile.framework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.support.toJsonObject

/**
 * Register account on blockchain node with credentials
 *
 * @author Daria Pechkovskaya
 */
class RegisterSocketOperation(
    override val apiId: Int,
    private val accountName: String,
    private val keyActive: String,
    private val echorandKey: String,
    callId: Int,
    callback: Callback<Int>
) : SocketOperation<Int>(SocketMethodType.CALL, callId, Int::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.REGISTER_ACCOUNT.key)

            add(JsonArray().apply {
                add(callId)
                add(accountName)
                add(keyActive)
                add(echorandKey)
            })
        }

    override fun fromJson(json: String): Int? {
        return json.toJsonObject()?.has(RESULT_KEY)?.let { callId } ?: -1
    }

}
