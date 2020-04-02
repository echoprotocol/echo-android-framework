package org.echo.mobile.framework.model.socketoperations

import com.google.common.primitives.UnsignedLong
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.support.toJsonObject

/**
 * Tries to submit registration task to blockchain with predefined [randNum] and solved [nonce]
 *
 * @author Dmitriy Bushuev
 */
class SubmitRegistrationSolutionSocketOperation(
    override val apiId: Int,
    private val accountName: String,
    private val keyActive: String,
    private val echorandKey: String,
    private val evmAddress: String?,
    private val nonce: UnsignedLong,
    private val randNum: UnsignedLong,
    callId: Int,
    callback: Callback<Int>
) : SocketOperation<Int>(SocketMethodType.CALL, callId, Int::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.SUBMIT_REGISTRATION_SOLUTION.key)

            add(JsonArray().apply {
                add(callId)
                add(accountName)
                add(keyActive)
                add(echorandKey)
                evmAddress?.let { add(it) }  ?: add(JsonNull.INSTANCE)
                add(nonce)
                add(randNum)
            })
        }

    override fun fromJson(json: String): Int? {
        val result = json.toJsonObject()?.get(RESULT_KEY)?.asBoolean
        return if (result == true) callId else -1
    }

}
