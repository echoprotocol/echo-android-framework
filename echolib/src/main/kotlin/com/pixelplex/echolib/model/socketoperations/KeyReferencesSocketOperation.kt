package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.model.Account
import com.pixelplex.echolib.support.Api
import com.pixelplex.echolib.support.getId

/**
 * Get all account references
 *
 * @return all accounts that refer to the key or account id in their owner or active authorities.
 *
 * @author Daria Pechkovskaya
 */
class KeyReferencesSocketOperation(
    val api: Api,
    val publicKey: String,
    method: SocketMethodType = SocketMethodType.CALL,
    callId: Int,
    callback: Callback<List<Account>>

) : SocketOperation<List<Account>>(
    method,
    callId,
    listOf<Account>().javaClass,
    callback
) {


    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.KEY_REFERENCES.key)

            val publicKeyJson = JsonArray().apply { add(publicKey) }
            add(JsonArray().apply { add(publicKeyJson) })
        }

    override val apiId: Int
        get() = api.getId()
}
