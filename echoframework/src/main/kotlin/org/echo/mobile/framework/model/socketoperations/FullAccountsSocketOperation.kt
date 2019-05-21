package org.echo.mobile.framework.model.socketoperations

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AccountOptions
import org.echo.mobile.framework.model.Balance
import org.echo.mobile.framework.model.FullAccount
import org.echo.mobile.framework.model.eddsa.EdAuthority
import org.echo.mobile.framework.model.network.Network

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
    override val apiId: Int,
    val namesOrIds: List<String>,
    val shouldSubscribe: Boolean,
    val network: Network,
    callId: Int,
    callback: Callback<Map<String, FullAccount>>
) : SocketOperation<Map<String, FullAccount>>(
    SocketMethodType.CALL,
    callId,
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

    override fun fromJson(json: String): Map<String, FullAccount> {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        val accountsMap = hashMapOf<String, FullAccount>()

        if (!jsonTree.isJsonObject || jsonTree.asJsonObject.get(RESULT_KEY) == null) {
            return accountsMap
        }

        try {
            val result = jsonTree.asJsonObject.get(RESULT_KEY)?.asJsonArray
            val size = result?.size() ?: 0

            val gson = configureGson()

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

    private fun configureGson() = GsonBuilder().apply {
        registerTypeAdapter(EdAuthority::class.java, EdAuthority.Deserializer())
        registerTypeAdapter(Account::class.java, Account.Deserializer())
        registerTypeAdapter(FullAccount::class.java, FullAccount.FullAccountDeserializer())
        registerTypeAdapter(Balance::class.java, Balance.BalanceDeserializer())
        registerTypeAdapter(
            AccountOptions::class.java,
            AccountOptions.Deserializer(network)
        )
    }.create()
}
