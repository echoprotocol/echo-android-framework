package org.echo.mobile.framework.service.internal.subscription

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.JsonElement
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.model.contract.ContractLog
import org.echo.mobile.framework.model.contract.Log
import org.echo.mobile.framework.model.contract.output.ContractAddressOutputValueType
import org.echo.mobile.framework.model.contract.output.ContractOutputDecoder
import org.echo.mobile.framework.model.contract.processType
import org.echo.mobile.framework.service.ContractSubscriptionManager
import org.echo.mobile.framework.service.UpdateListener
import org.echo.mobile.framework.support.toJsonObject
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of [ContractSubscriptionManager]
 *
 * @author Dmitriy Bushuev
 */
class ContractSubscriptionManagerImpl : ContractSubscriptionManager {

    private val listeners = ConcurrentHashMap<String, MutableList<UpdateListener<List<ContractLog>>>>()

    private val gson = Gson()
    private val type = object : TypeToken<List<ContractLog>>() {}.type

    private val contractDecoder = ContractOutputDecoder()

    override fun registerListener(id: String, listener: UpdateListener<List<ContractLog>>) {
        val accountListeners = listeners[id]

        if (accountListeners == null) {
            val listenersByName = mutableListOf(listener)
            listeners[id] = listenersByName
        } else {
            accountListeners += listener
        }
    }

    override fun containsListeners(): Boolean = listeners.isNotEmpty()

    override fun registered(id: String): Boolean = listeners.containsKey(id)

    override fun removeListeners(id: String): MutableList<UpdateListener<List<ContractLog>>>? =
        listeners.remove(id)

    override fun clear() = listeners.clear()

    override fun notify(contractId: String, logs: List<ContractLog>) {
        listeners[contractId]?.forEach { listener ->
            listener.onUpdate(logs)
        }
    }

    override fun processEvent(event: String): Map<String, List<ContractLog>> {
        try {
            val dataParam = getDataParam(event)?.asJsonArray

            val logsJsonArray = dataParam?.firstOrNull()?.asJsonArray

            val logsMap = mutableMapOf<String, MutableList<ContractLog>>()
            logsJsonArray?.forEach { logArray ->
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

                contractLog.address= address

                logsMap[address]?.add(contractLog) ?: let {
                    logsMap[address] = mutableListOf(contractLog)
                }
            }

            return logsMap
        } catch (ex: Exception) {
            return emptyMap()
        }
    }

    private fun getDataParam(event: String): JsonElement? {
        val params = event.toJsonObject()?.getAsJsonArray(PARAMS_KEY) ?: return null

        if (params.size() == 0) {
            return null
        }

        return params[1].asJsonArray
    }

    companion object {
        private const val PARAMS_KEY = "params"
        private val LOGGER =
            LoggerCoreComponent.create(ContractSubscriptionManagerImpl::class.java.name)
    }


}
