package org.echo.mobile.framework.model.socketoperations

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.BalanceObject

/**
 * This function fetches all unclaimed balance objects for a set of public keys
 *
 * @param publicKeys public keys array
 *
 * @returns array of balance object [BalanceObject]
 *
 * @author Pavel Savchkov
 */
class GetBalanceObjectsSocketOperation(
        override val apiId: Int,
        val publicKeys: List<String>,
        callId: Int,
        callback: Callback<BalanceObject>
) : SocketOperation<BalanceObject>(
        SocketMethodType.CALL,
        callId,
        BalanceObject::class.java,
        callback
) {

    override fun createParameters(): JsonElement =
            JsonArray().apply {
                add(apiId)
                add(SocketOperationKeys.GET_BALANCE_OBJECTS.key)

                val dataJson = JsonArray()

                val publicKeysJsonArray = JsonArray()
                publicKeys.forEach { publicKeysJsonArray.add(it) }

                dataJson.add(publicKeysJsonArray)

                add(JsonArray().apply { addAll(dataJson) })
            }

    override fun fromJson(json: String): BalanceObject? {
        val responseType = object : TypeToken<BalanceObject>() {}.type

        return configureGson()
                .fromJson<BalanceObject>(json, responseType)
    }

    private fun configureGson() = GsonBuilder().apply {
        registerTypeAdapter(BalanceObject::class.java, BalanceObject.BalanceDeserializer())
    }.create()

}
