package org.echo.mobile.framework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.ILLEGAL_ID
import org.echo.mobile.framework.model.Account

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
    callId: Int,
    callback: Callback<List<Account>>

) : SocketOperation<List<Account>>(
    SocketMethodType.CALL,
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

    override fun fromJson(json: String): List<Account>? {
        return null
    }
}
