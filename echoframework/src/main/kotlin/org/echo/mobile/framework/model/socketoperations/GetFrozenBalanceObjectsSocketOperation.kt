package org.echo.mobile.framework.model.socketoperations

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.FrozenBalanceObject

/**
 * Return an array of frozen balances for a set of addresses ID.
 *
 * @param accountId id of the account
 *
 * @returns array of balance object [FrozenBalanceObject]
 *
 * @author Pavel Savchkov
 */
class GetFrozenBalanceObjectsSocketOperation(
        override val apiId: Int,
        val accountId: String,
        callId: Int,
        callback: Callback<FrozenBalanceObject>
) : SocketOperation<FrozenBalanceObject>(
        SocketMethodType.CALL,
        callId,
        FrozenBalanceObject::class.java,
        callback
) {

    override fun createParameters(): JsonElement =
            JsonArray().apply {
                add(apiId)
                add(SocketOperationKeys.GET_FROZEN_BALANCES.key)

                val dataJson = JsonArray()

                dataJson.add(accountId)

                add(JsonArray().apply { addAll(dataJson) })
            }

    override fun fromJson(json: String): FrozenBalanceObject? {
        val responseType = object : TypeToken<FrozenBalanceObject>() {}.type

        return configureGson()
                .fromJson<FrozenBalanceObject>(json, responseType)
    }

    private fun configureGson() = GsonBuilder().apply {
        registerTypeAdapter(FrozenBalanceObject::class.java, FrozenBalanceObject.FrozenBalanceDeserializer())
    }.create()

}
