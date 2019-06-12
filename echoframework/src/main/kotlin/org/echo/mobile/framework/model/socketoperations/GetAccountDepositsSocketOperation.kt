package org.echo.mobile.framework.model.socketoperations

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.EthDeposit

/**
 * Retrieves ethereum deposits by [accountId]
 *
 * @author Dmitriy Bushuev
 */
class GetAccountDepositsSocketOperation(
    override val apiId: Int,
    val accountId: String,
    callId: Int,
    callback: Callback<List<EthDeposit>>
) : SocketOperation<List<EthDeposit>>(
    SocketMethodType.CALL,
    callId,
    listOf<EthDeposit>().javaClass,
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

    override fun fromJson(json: String): List<EthDeposit> {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        if (!jsonTree.isJsonObject || jsonTree.asJsonObject.get(RESULT_KEY).isJsonNull) {
            return listOf()
        }

        val type = object : TypeToken<List<EthDeposit>>() {}.type

        val result = jsonTree.asJsonObject.get(RESULT_KEY)

        return Gson().fromJson(result, type)
    }

}
