package org.echo.mobile.framework.model.socketoperations

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.ERC20Deposit

/**
 * Retrieves erc20 deposits for [accountId]
 *
 * @author Dmitriy Bushuev
 */
class GetERC20DepositsSocketOperation(
    override val apiId: Int,
    val accountId: String,
    callId: Int,
    callback: Callback<List<ERC20Deposit>>
) : SocketOperation<List<ERC20Deposit>>(
    SocketMethodType.CALL,
    callId,
    listOf<ERC20Deposit>().javaClass,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.GET_ERC20_TOKEN_DEPOSITS.key)
            add(JsonArray().apply {
                add(accountId)
            })
        }

    override fun fromJson(json: String): List<ERC20Deposit> {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        if (!jsonTree.isJsonObject || jsonTree.asJsonObject.get(RESULT_KEY).isJsonNull) {
            return listOf()
        }

        val depositListJson = jsonTree.asJsonObject.get(RESULT_KEY).asJsonArray

        return Gson().fromJson(depositListJson, object : TypeToken<List<ERC20Deposit>>() {}.type)
    }

}
