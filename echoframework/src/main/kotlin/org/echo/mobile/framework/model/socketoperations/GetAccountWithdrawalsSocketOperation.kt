package org.echo.mobile.framework.model.socketoperations

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.EthWithdraw

/**
 * Retrieves ethereum withdrawals by [accountId]
 *
 * @author Dmitriy Bushuev
 */
class GetAccountWithdrawalsSocketOperation(
    override val apiId: Int,
    val accountId: String,
    callId: Int,
    callback: Callback<List<EthWithdraw>>
) : SocketOperation<List<EthWithdraw>>(
    SocketMethodType.CALL,
    callId,
    listOf<EthWithdraw>().javaClass,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.GET_ACCOUNT_DEPOSITS.key)
            add(JsonArray().apply {
                add(accountId)
            })
        }

    override fun fromJson(json: String): List<EthWithdraw> {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        if (!jsonTree.isJsonObject || jsonTree.asJsonObject.get(RESULT_KEY).isJsonNull) {
            return listOf()
        }

        val type = object : TypeToken<List<EthWithdraw>>() {}.type

        val result = jsonTree.asJsonObject.get(RESULT_KEY)

        return Gson().fromJson(result, type)
    }

}
