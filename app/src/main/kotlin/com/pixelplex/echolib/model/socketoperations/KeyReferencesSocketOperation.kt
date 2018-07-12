package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement

/**
 * Get all account references
 *
 * @return all accounts that refer to the key or account id in their owner or active authorities.
 *
 * @author Daria Pechkovskaya
 */
class KeyReferencesSocketOperation(
    method: SocketMethodType = SocketMethodType.CALL,
    callId: Int,
    apiId: Int,
    result: OperationResult<*>,
    val publicKey: String
) : SocketOperation(method, callId, apiId, result) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.KEY_REFERENCES.key)

            val publicKeyJson = JsonArray().apply { add(publicKey) }
            add(JsonArray().apply { add(publicKeyJson) })
        }

}
