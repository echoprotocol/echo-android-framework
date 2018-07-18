package com.pixelplex.echolib.model.socketoperations

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.ILLEGAL_ID
import com.pixelplex.echolib.model.Account
import com.pixelplex.echolib.model.Authority
import com.pixelplex.echolib.model.FullAccount
import com.pixelplex.echolib.model.network.Network
import com.pixelplex.echolib.support.Api
import com.pixelplex.echolib.support.getId

/**
 * This function fetches all relevant [Account] objects for the given accounts, and
 * subscribes to updates to the given accounts. If any of the strings in [namesOrIds] cannot be
 * tied to an account, that input will be ignored. All other accounts will be retrieved and
 * subscribed.
 *
 * @param namesOrIds Each item must be the name or ID of an account to retrieve
 * @param shouldSubscribe Flag of subscription on updates
 *
 * @author Daria Pechkovskaya
 */
class FullAccountsSocketOperation(
    val api: Api,
    val namesOrIds: List<String>,
    val shouldSubscribe: Boolean,
    val network: Network,
    method: SocketMethodType = SocketMethodType.CALL,
    callback: Callback<Map<String, FullAccount>>
) : SocketOperation<Map<String, FullAccount>>(
    method,
    ILLEGAL_ID,
    mapOf<String, FullAccount>().javaClass,
    callback
) {

    @Suppress("UNUSED_EXPRESSION")
    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.FULL_ACCOUNTS.key)

            val dataJson = JsonArray()

            val idsJson = JsonArray()
            namesOrIds.forEach { item -> idsJson.add(item) }
            dataJson.add(idsJson)
            dataJson.add(shouldSubscribe)

            add(JsonArray().apply { addAll(dataJson) })
        }

    override val apiId: Int
        get() = api.getId()

    override fun fromJson(json: String): Map<String, FullAccount> {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        val accountsMap = hashMapOf<String, FullAccount>()

        if (!jsonTree.isJsonObject || jsonTree.asJsonObject.get("result") == null) {
            return accountsMap
        }

        try {
            val result = jsonTree.asJsonObject.get("result")?.asJsonArray
            val size = result?.size() ?: 0

            val gson = GsonBuilder()
                .registerTypeAdapter(Authority::class.java, Authority.Deserializer(network))
                .create()

            for (i in 0 until size) {
                val subArray = result!!.get(i).asJsonArray
                val id = subArray.get(0).asString
                val accObj = subArray.get(1).asJsonObject
                val fullAccount = gson.fromJson<FullAccount>(accObj, FullAccount::class.java)
                fullAccount?.let { acc ->
                    accountsMap[id] = acc
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return accountsMap
    }
}
