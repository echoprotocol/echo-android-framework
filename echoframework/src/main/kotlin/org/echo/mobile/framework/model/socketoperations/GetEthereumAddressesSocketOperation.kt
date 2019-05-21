package org.echo.mobile.framework.model.socketoperations

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.EthAddress

/**
 * Retrieves ethereum addresses list for required account
 *
 * @param accountId Required account id
 *
 * @author Dmitriy Bushuev
 */
class GetEthereumAddressesSocketOperation(
    override val apiId: Int,
    val accountId: String,
    callId: Int,
    callback: Callback<List<EthAddress>>
) : SocketOperation<List<EthAddress>>(
    SocketMethodType.CALL,
    callId,
    listOf<EthAddress>().javaClass,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.GET_ACCOUNT_ADDRESSES.key)
            add(JsonArray().apply {
                add(accountId)
            })
        }

    override fun fromJson(json: String): List<EthAddress> {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        val addresses = mutableListOf<EthAddress>()

        if (!jsonTree.isJsonObject || jsonTree.asJsonObject.get(RESULT_KEY) == null) {
            return addresses
        }

        val responseType = object : TypeToken<List<EthAddress>>() {}.type
        val result = jsonTree.asJsonObject.get(RESULT_KEY)?.asJsonArray

        return Gson().fromJson(result, responseType)
    }

}
