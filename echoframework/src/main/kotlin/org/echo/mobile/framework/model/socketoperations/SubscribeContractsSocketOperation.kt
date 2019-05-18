package org.echo.mobile.framework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.support.toJsonObject

/**
 * Register subscription callback to contracts changes (history or object changes).
 * Every notification initiated by the full node will carry a particular id as defined by the
 * user with the identifier parameter.
 *
 * @param contractIds Id of contracts for subscription
 * @return List of changes
 *
 * @author Daria Pechkovskaya
 */
class SubscribeContractsSocketOperation(
    override val apiId: Int,
    private val contractIds: List<String>,
    callId: Int,
    callback: Callback<Boolean>
) : SocketOperation<Boolean>(SocketMethodType.CALL, callId, Boolean::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.SUBSCRIBE_CONTRACTS.key)
            add(JsonArray().apply {
                val idsJson = JsonArray().apply {
                    contractIds.forEach { item -> add(item) }
                }
                add(idsJson)
            })
        }

    override fun fromJson(json: String): Boolean =
        json.toJsonObject()?.has(RESULT_KEY) ?: false

}
