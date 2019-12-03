package org.echo.mobile.framework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.SidechainType
import org.echo.mobile.framework.model.Withdraw
import org.echo.mobile.framework.model.WithdrawMapper

/**
 * Retrieves ethereum withdrawals by [accountId]
 *
 * @author Dmitriy Bushuev
 */
class GetAccountWithdrawalsSocketOperation(
    override val apiId: Int,
    val accountId: String,
    private val sidechainType: SidechainType?,
    callId: Int,
    callback: Callback<List<Withdraw?>>
) : SocketOperation<List<Withdraw?>>(
    SocketMethodType.CALL,
    callId,
    listOf<Withdraw?>().javaClass,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.GET_ACCOUNT_WITHDRAWALS.key)
            add(JsonArray().apply {
                add(accountId)
                add(sidechainType?.name?.toLowerCase() ?: "")
            })
        }

    override fun fromJson(json: String): List<Withdraw?> {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        if (!jsonTree.isJsonObject || jsonTree.asJsonObject.get(RESULT_KEY).isJsonNull) {
            return listOf()
        }

        val depositListJson = jsonTree.asJsonObject.get(RESULT_KEY).asJsonArray

        val mapper = WithdrawMapper()
        return depositListJson.map { it.toString() }.map { candidate ->
            mapper.map(candidate)
        }
    }

}
