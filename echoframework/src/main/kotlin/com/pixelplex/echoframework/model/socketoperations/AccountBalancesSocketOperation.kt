package com.pixelplex.echoframework.model.socketoperations

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.ILLEGAL_ID
import com.pixelplex.echoframework.model.Account
import com.pixelplex.echoframework.model.AssetAmount

/**
 * This function fetches all relevant [Account] objects for the given accounts, and
 * subscribes to updates to the given accounts. If any of the strings in [nameOrId] cannot be
 * tied to an account, that input will be ignored. All other accounts will be retrieved and
 * subscribed.
 *
 * @param nameOrId Object if of required account!
 * @param asset Required asset type
 * @param shouldSubscribe Flag of subscription on updates
 *
 * @author Daria Pechkovskaya
 */
class AccountBalancesSocketOperation(
    override val apiId: Int,
    val nameOrId: String,
    val asset: String,
    val shouldSubscribe: Boolean,
    callId: Int,
    callback: Callback<AssetAmount>
) : SocketOperation<AssetAmount>(
    SocketMethodType.CALL,
    callId,
    AssetAmount::class.java,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.ACCOUNT_BALANCES.key)

            val dataJson = JsonArray()

            dataJson.add(nameOrId)

            val assetsJson = JsonArray()

            assetsJson.add(asset)

            dataJson.add(assetsJson)
            dataJson.add(shouldSubscribe)

            add(JsonArray().apply { addAll(dataJson) })
        }

    override fun fromJson(json: String): AssetAmount? {
        val responseType = object : TypeToken<AssetAmount>() {}.type

        return configureGson()
            .fromJson<AssetAmount>(json, responseType)
    }

    private fun configureGson() = GsonBuilder().apply {
        registerTypeAdapter(AssetAmount::class.java, AssetAmount.Deserializer())
    }.create()

}
