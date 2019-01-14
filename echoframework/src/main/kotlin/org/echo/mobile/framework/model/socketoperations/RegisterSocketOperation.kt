package org.echo.mobile.framework.model.socketoperations

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AccountOptions
import org.echo.mobile.framework.model.Authority
import org.echo.mobile.framework.model.Balance
import org.echo.mobile.framework.model.FullAccount
import org.echo.mobile.framework.model.network.Network

/**
 * Register account on blockchain node with credentials
 *
 * @author Daria Pechkovskaya
 */
class RegisterSocketOperation(
    override val apiId: Int,
    private val accountName: String,
    private val keyOwner: String,
    private val keyActive: String,
    private val keyMemo: String,
    private val echorandKey: String,
    private val network: Network,
    callId: Int,
    callback: Callback<FullAccount>
) : SocketOperation<FullAccount>(SocketMethodType.CALL, callId, FullAccount::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.REGISTER_ACCOUNT.key)

            add(JsonArray().apply {
                add(accountName)
                add(keyOwner)
                add(keyActive)
                add(keyMemo)
                add(echorandKey)
            })
        }

    override fun fromJson(json: String): FullAccount? {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        if (!jsonTree.isJsonObject || jsonTree.asJsonObject.get(RESULT_KEY) == null) {
            return null
        }

        try {
            val result = jsonTree.asJsonObject.get(RESULT_KEY)?.asJsonObject

            val gson = configureGson()

            val subArray = result!!.asJsonArray
            val id = subArray.get(0).asString
            val accObj = subArray.get(1).asJsonObject

            return gson.fromJson<FullAccount>(accObj, FullAccount::class.java)

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return null
    }

    private fun configureGson() = GsonBuilder().apply {
        registerTypeAdapter(Authority::class.java, Authority.Deserializer(network))
        registerTypeAdapter(Account::class.java, Account.Deserializer())
        registerTypeAdapter(FullAccount::class.java, FullAccount.FullAccountDeserializer())
        registerTypeAdapter(Balance::class.java, Balance.BalanceDeserializer())
        registerTypeAdapter(
            AccountOptions::class.java,
            AccountOptions.Deserializer(network)
        )
    }.create()
}
