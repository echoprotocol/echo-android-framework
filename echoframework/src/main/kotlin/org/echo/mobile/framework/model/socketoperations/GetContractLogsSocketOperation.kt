package org.echo.mobile.framework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.support.toJsonObject

/**
 * Retrieves emitted logs of contract
 *
 * @param contractId Contract id for fetching logs
 * @param fromBlock Number of the earliest block to retrieve
 * @param toBlock Number of the most recent block to retrieve
 *
 * @author Daria Pechkovskaya
 */
class GetContractLogsSocketOperation(
    override val apiId: Int,
    private val contractId: String,
    private val fromBlock: String,
    private val toBlock: String,
    callId: Int,
    callback: Callback<Int>

) : SocketOperation<Int>(
    SocketMethodType.CALL,
    callId,
    Int::class.java,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.GET_CONTRACT_LOGS.key)
            add(JsonArray().apply {
                add(callId)
                add(configureOptions())
            })
        }

    private fun configureOptions(): JsonObject {
        val options = JsonObject()

        options.add(CONTRACTS_KEY, JsonArray().apply { add(contractId) })
        options.add(TOPICS_KEY, JsonArray())
        options.addProperty(FROM_BLOCK_KEY, fromBlock)
        options.addProperty(TO_BLOCK_KEY, toBlock)

        return options
    }

    override fun fromJson(json: String): Int {
        return json.toJsonObject()?.has(PARAMS_KEY)?.let { callId } ?: -1
    }

    companion object {
        private const val PARAMS_KEY = "result"

        private const val CONTRACTS_KEY = "contracts"
        private const val TOPICS_KEY = "topics"
        private const val FROM_BLOCK_KEY = "from_block"
        private const val TO_BLOCK_KEY = "to_block"
    }

}
