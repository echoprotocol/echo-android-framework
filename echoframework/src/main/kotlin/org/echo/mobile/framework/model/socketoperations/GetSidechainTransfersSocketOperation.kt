package org.echo.mobile.framework.model.socketoperations

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.ILLEGAL_ID
import org.echo.mobile.framework.model.SidechainTransfer

/**
 * Get a list of assets by symbol
 *
 * @param lowerBound Asset symbol
 * @param limit Assets count limit
 * @return List of required assets
 *
 * @author Bushuev Dmitriy
 */
class GetSidechainTransfersSocketOperation(
    override val apiId: Int,
    val ethId: String,
    method: SocketMethodType = SocketMethodType.CALL,
    callback: Callback<List<SidechainTransfer>>

) : SocketOperation<List<SidechainTransfer>>(
    method,
    ILLEGAL_ID,
    listOf<SidechainTransfer>().javaClass,
    callback
) {

    @Suppress("UNUSED_EXPRESSION")
    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.SIDECHAIN_TRANSFERS.key)

            add(JsonArray().apply {
                add(ethId)
            })
        }

    override fun fromJson(json: String): List<SidechainTransfer> {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        val result = jsonTree.asJsonObject.get(RESULT_KEY)?.asJsonArray
            ?: return emptyList()

        val gson = GsonBuilder().create()

        return gson.fromJson<List<SidechainTransfer>>(
            result,
            object : TypeToken<List<SidechainTransfer>>() {}.type
        )
    }

    companion object {
        private const val RESULT_KEY = "result"
    }

}
