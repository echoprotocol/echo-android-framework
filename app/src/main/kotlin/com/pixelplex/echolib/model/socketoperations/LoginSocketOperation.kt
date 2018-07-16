package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.pixelplex.echolib.Callback

/**
 * Represents blockchain call for access to blockchain
 *
 * @author Daria Pechkovskaya
 */
class LoginSocketOperation(
    val api: Int,
    method: SocketMethodType = SocketMethodType.CALL,
    callId: Int = -1,
    callback: Callback<Boolean>
) : SocketOperation<Boolean>(method, callId, Boolean::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.LOGIN.key)
            add(getParameters())
        }

    override val apiId: Int
        get() = api

    private fun getParameters(): JsonElement =
        JsonArray().apply {
            add("")
            add("")
        }
}
