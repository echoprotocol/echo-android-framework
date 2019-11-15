package org.echo.mobile.framework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.contract.ContractLog
import org.echo.mobile.framework.model.contract.Log
import org.echo.mobile.framework.model.contract.output.ContractAddressOutputValueType
import org.echo.mobile.framework.model.contract.output.ContractOutputDecoder
import org.echo.mobile.framework.model.contract.processType
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
    callback: Callback<List<ContractLog>>

) : SocketOperation<List<ContractLog>>(
    SocketMethodType.CALL,
    callId,
    listOf<ContractLog>().javaClass,
    callback
) {

    private val contractDecoder = ContractOutputDecoder()

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.GET_CONTRACT_LOGS.key)
            add(JsonArray().apply {
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

    override fun fromJson(json: String): List<ContractLog> {
        val dataParam = getDataParam(json)?.asJsonArray

        val logs = mutableListOf<ContractLog>()
        dataParam?.forEach { logArray ->
            val contractArray = logArray.asJsonArray
            val type = contractArray[0]
            val contractJson = logArray.asJsonArray[1]

            val log = Log(type.asInt, contractJson.toString())

            val contractLog = log.processType()

            val decodedValues = contractDecoder.decode(
                contractLog?.address!!.toByteArray(),
                listOf(ContractAddressOutputValueType())
            )

            val address = decodedValues.first().value.toString()

            contractLog.address = address

            logs.add(contractLog)
        }

        return logs
    }

    private fun getDataParam(event: String): JsonElement? {
        val params = event.toJsonObject()?.getAsJsonArray(PARAMS_KEY) ?: return null

        if (params.size() == 0) {
            return null
        }

        return params
    }

    companion object {
        private const val PARAMS_KEY = "result"

        private const val CONTRACTS_KEY = "contracts"
        private const val TOPICS_KEY = "topics"
        private const val FROM_BLOCK_KEY = "from_block"
        private const val TO_BLOCK_KEY = "to_block"
    }

}
