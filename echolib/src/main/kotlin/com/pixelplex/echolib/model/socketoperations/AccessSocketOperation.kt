package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.pixelplex.echolib.Callback

/**
 * Represents blockchain call for access to blockchain apis
 *
 * @author Daria Pechkovskaya
 */
class AccessSocketOperation(
    val accessSocketType: AccessSocketOperationType,
    val api: Int,
    method: SocketMethodType = SocketMethodType.CALL,
    callId: Int = -1,
    callback: Callback<Int>
) : SocketOperation<Int>(method, callId, Int::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(accessSocketType.key)
            add(JsonArray())
        }

    override val apiId: Int
        get() = api
}

/**
 * Type of operation for access to blockchain
 */
enum class AccessSocketOperationType(val key: String) {
    DATABASE("database"),
    NETWORK_BROADCAST("network_broadcast"),
    HISTORY("history"),
    CRYPTO("crypto"),
    NETWORK_NODES("network_nodes")
}
