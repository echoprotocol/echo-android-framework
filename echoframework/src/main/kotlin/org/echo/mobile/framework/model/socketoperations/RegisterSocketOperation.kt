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
    private val keyOwner: String,
    private val keyActive: String,
    private val keyMemo: String,
    private val echorandKey: String,
    callId: Int,
    callback: Callback<Boolean>
) : SocketOperation<Boolean>(SocketMethodType.CALL, callId, Boolean::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.REGISTER_ACCOUNT.key)

            add(JsonArray().apply {
                add(accountName)
                add(keyOwner)
                add(keyActive)
                add(keyMemo)
                add(echorandKey)
            })
        }

    override fun fromJson(json: String): Boolean? {
        return json.toJsonObject()?.has(RESULT_KEY) ?: false
    }

}
