package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement

/**
 * Represents blockchain call for access to blockchain apis
 *
 * @author Daria Pechkovskaya
 */
class AccessSocketOperation(
    val type: AccessSocketOperationType,
    method: SocketMethodType = SocketMethodType.CALL,
    callId: Int,
    apiId: Int,
    result: OperationResult<*>
) : SocketOperation(method, callId, apiId, result) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(type.key)
            add(getParameters(type))
        }

    private fun getParameters(type: AccessSocketOperationType): JsonElement =
        when (type) {
            AccessSocketOperationType.LOGIN -> JsonArray().apply {
                add("")
                add("")
            }
            else -> JsonArray()
        }
}

/**
 * Type of operation for access to blockchain
 */
enum class AccessSocketOperationType(val key: String) {
    LOGIN("login"),
    DATABASE("database"),
    NETWORK_BROADCAST("network_broadcast"),
    HISTORY("history"),
    CRYPTO("crypto"),
    NETWORK_NODES("network_nodes")
}
