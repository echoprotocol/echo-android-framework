package org.echo.mobile.framework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.support.toJsonObject

/**
 * Register subscription callback to contract logs.
 * Every notification initiated by the full node will carry a particular id as defined by the
 * user with the identifier parameter.
 *
 * @param contractId Contract id for subscribe to logs
 * @return List of last logs
 *
 * @author Daria Pechkovskaya
 */
class SubscribeContractLogsSocketOperation(
    override val apiId: Int,
    private val contractId: String,
    callId: Int,
    callback: Callback<Boolean>
) : SocketOperation<Boolean>(SocketMethodType.CALL, callId, Boolean::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.SUBSCRIBE_CONTRACT_LOGS.key)
            add(JsonArray().apply {
                add(callId)
                add(JsonArray().apply {
                    add(JsonArray().apply {
                        add(contractId)
                        add(JsonArray())
                    })
                })
            })
        }

    override fun fromJson(json: String): Boolean? {
        return json.toJsonObject()?.has(RESULT_KEY) ?: false
    }

}
