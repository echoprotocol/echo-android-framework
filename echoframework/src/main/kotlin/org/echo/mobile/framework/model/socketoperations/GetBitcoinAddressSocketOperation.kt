package org.echo.mobile.framework.model.socketoperations

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.BtcAddress
import org.echo.mobile.framework.model.EthAddress

/**
 * Retrieves bitcoin address for required account
 *
 * @param accountId Required account id
 *
 * @author Dmitriy Bushuev
 */
class GetBitcoinAddressSocketOperation(
    override val apiId: Int,
    val accountId: String,
    callId: Int,
    callback: Callback<BtcAddress>
) : SocketOperation<BtcAddress>(
    SocketMethodType.CALL,
    callId,
    BtcAddress::class.java,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.GET_BTC_ADDRESS.key)
            add(JsonArray().apply {
                add(accountId)
            })
        }

    override fun fromJson(json: String): BtcAddress? {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        if (!jsonTree.isJsonObject || jsonTree.asJsonObject.get(RESULT_KEY).isJsonNull) {
            return null
        }

        val result = jsonTree.asJsonObject.get(RESULT_KEY)

        return Gson().fromJson(result, BtcAddress::class.java)
    }

}
