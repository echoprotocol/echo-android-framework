package com.pixelplex.echoframework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.ILLEGAL_ID
import com.pixelplex.echoframework.model.Account

/**
 * Get all account references
 *
 * @return all accounts that refer to the key or account id in their owner or active authorities.
 *
 * @author Daria Pechkovskaya
 */
class KeyReferencesSocketOperation(
    override val apiId: Int,
    val publicKey: String,
    method: SocketMethodType = SocketMethodType.CALL,
    callback: Callback<List<Account>>

) : SocketOperation<List<Account>>(
    method,
    ILLEGAL_ID,
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

    override fun fromJson(json: String): List<Account>? {
        return null
    }
}
