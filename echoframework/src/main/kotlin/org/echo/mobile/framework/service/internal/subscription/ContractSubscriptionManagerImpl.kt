package org.echo.mobile.framework.service.internal.subscription

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import org.echo.mobile.framework.model.Log
import org.echo.mobile.framework.model.contract.output.AddressOutputValueType
import org.echo.mobile.framework.model.contract.output.ContractAddressOutputValueType
import org.echo.mobile.framework.model.contract.output.ContractOutputDecoder
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

    private val listeners = ConcurrentHashMap<String, MutableList<UpdateListener<List<Log>>>>()

    private val gson = Gson()
    private val type = object : TypeToken<List<Log>>() {}.type

    private val contractDecoder = ContractOutputDecoder()

    override fun registerListener(id: String, listener: UpdateListener<List<Log>>) {
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

    override fun removeListeners(id: String): MutableList<UpdateListener<List<Log>>>? =
        listeners.remove(id)

    override fun clear() = listeners.clear()

    override fun notify(contractId: String, logs: List<Log>) {
        listeners[contractId]?.forEach { listener ->
            listener.onUpdate(logs)
        }
    }

    override fun processEvent(event: String): Map<String, List<Log>> {
        try {
            val dataParam = getDataParam(event)?.asJsonArray

            val logsJsonArray = dataParam?.firstOrNull()?.asJsonArray
            val logs = gson.fromJson<List<Log>>(logsJsonArray, type)

            val logsMap = mutableMapOf<String, MutableList<Log>>()
            logs.forEach { log ->

                val decodedValues = contractDecoder.decode(
                    log.address.toByteArray(),
                    listOf(ContractAddressOutputValueType())
                )

                val address = decodedValues.first().value.toString()

                logsMap[address]?.add(log) ?: let {
                    logsMap[address] = mutableListOf(log)
                }
            }

            return logsMap
        } catch (ex: JsonParseException) {
            ex.printStackTrace()
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
    }

}
