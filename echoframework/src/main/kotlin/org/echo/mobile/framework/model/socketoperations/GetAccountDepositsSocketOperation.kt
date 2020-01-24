package org.echo.mobile.framework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.Deposit
import org.echo.mobile.framework.model.DepositMapper
import org.echo.mobile.framework.model.SidechainType

/**
 * Retrieves ethereum deposits by [accountId]
 *
 * @author Dmitriy Bushuev
 */
class GetAccountDepositsSocketOperation(
    override val apiId: Int,
    val accountId: String,
    val sidechainType: SidechainType?,
    callId: Int,
    callback: Callback<List<Deposit?>>
) : SocketOperation<List<Deposit?>>(
    SocketMethodType.CALL,
    callId,
    listOf<Deposit>().javaClass,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.GET_ACCOUNT_DEPOSITS.key)
            add(JsonArray().apply {
                add(accountId)
                add(sidechainType?.name?.toLowerCase() ?: "")
            })
        }

    override fun fromJson(json: String): List<Deposit?> {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        if (!jsonTree.isJsonObject || jsonTree.asJsonObject.get(RESULT_KEY).isJsonNull) {
            return listOf()
        }

        val depositListJson = jsonTree.asJsonObject.get(RESULT_KEY).asJsonArray

        val mapper = DepositMapper()
        return depositListJson.map { it.toString() }.map { candidate ->
            mapper.map(candidate)
        }
    }

}
