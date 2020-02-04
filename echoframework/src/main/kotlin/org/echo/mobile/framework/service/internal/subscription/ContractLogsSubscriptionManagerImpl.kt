package org.echo.mobile.framework.service.internal.subscription

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.contract.ContractLog
import org.echo.mobile.framework.model.contract.Log
import org.echo.mobile.framework.model.contract.output.ContractAddressOutputValueType
import org.echo.mobile.framework.model.contract.output.ContractOutputDecoder
import org.echo.mobile.framework.model.contract.processType
import org.echo.mobile.framework.service.ContractLogsSubscriptionManager
import org.echo.mobile.framework.support.toJsonObject
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of [ContractLogsSubscriptionManager]
 *
 * @author Daria Pechkovskaya
 */
class ContractLogsSubscriptionManagerImpl : ContractLogsSubscriptionManager {

    private val callbacks =
        ConcurrentHashMap<String, Callback<List<ContractLog>>>()

    private val contractDecoder = ContractOutputDecoder()

    override fun register(callId: String, callback: Callback<List<ContractLog>>) {
        callbacks[callId] = callback
    }

    override fun clear() = callbacks.clear()

    override fun tryProcessEvent(event: String) {
        val (callId, dataParam) = getDataParam(event) ?: return
        val callback = callbacks.remove(callId)

        try {
            val jsonResult = dataParam?.asJsonArray?.firstOrNull()

            if (jsonResult?.isJsonArray == true) {
                val result = processContractLogsJson(jsonResult.asJsonArray)

                callback?.onSuccess(result) ?: return
            }

        } catch (ex: Exception) {
            LOGGER.log("Error while parsing contract logs result.", ex)
            callback?.onError(LocalException("Error while parsing contract logs result.", ex))
                ?: return
        }
    }

    private fun getDataParam(event: String): Pair<String, JsonElement?>? {
        val params = event.toJsonObject()?.getAsJsonArray(PARAMS_KEY) ?: return null

        if (params.size() == 0) return null

        return params[0].toString() to params[1].asJsonArray
    }

    private fun processContractLogsJson(dataParam: JsonArray): List<ContractLog>{
        val logs = mutableListOf<ContractLog>()
        dataParam.forEach { logArray ->
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

    companion object {
        private const val PARAMS_KEY = "params"
        private val LOGGER =
            LoggerCoreComponent.create(ContractLogsSubscriptionManagerImpl::class.java.name)
    }

}
