package org.echo.mobile.framework.service.internal.subscription

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.model.contract.ContractBalance
import org.echo.mobile.framework.service.ContractBalancesSubscriptionManager
import org.echo.mobile.framework.service.ContractSubscriptionManager
import org.echo.mobile.framework.service.UpdateListener
import org.echo.mobile.framework.support.toJsonObject
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Implementation of [ContractSubscriptionManager]
 *
 * @author Daria Pechkovskaya
 */
class ContractBalancesSubscriptionManagerImpl : ContractBalancesSubscriptionManager {

    private val listeners =
        CopyOnWriteArrayList<UpdateListener<Map<String, List<ContractBalance>>>>()

    private val gson by lazy { configureGson() }
    private val type = object : TypeToken<List<ContractBalance>>() {}.type

    override fun addListener(
        listener: UpdateListener<Map<String, List<ContractBalance>>>
    ) {
        listeners.add(listener)
    }

    override fun containsListeners(): Boolean = listeners.isNotEmpty()

    override fun registered(listener: UpdateListener<Map<String, List<ContractBalance>>>): Boolean =
        listeners.contains(listener)

    override fun removeListener(listener: UpdateListener<Map<String, List<ContractBalance>>>) =
        listeners.remove(listener)

    override fun clear() = listeners.clear()

    override fun notify(data: Map<String, List<ContractBalance>>) {
        listeners.forEach { listener ->
            listener.onUpdate(data)
        }
    }

    override fun processEvent(event: String): Map<String, List<ContractBalance>>? {
        try {
            val dataParam = getDataParam(event)?.asJsonArray

            val balancesJsonArray = dataParam?.firstOrNull()?.asJsonArray
            val balances = gson.fromJson<List<ContractBalance>>(balancesJsonArray, type)

            val balancesMap = mutableMapOf<String, MutableList<ContractBalance>>()
            balances.forEach { balance ->
                balance.contract?.getObjectId()?.let { contractId ->
                    balancesMap[contractId]?.add(balance)
                        ?: let { balancesMap[contractId] = mutableListOf(balance) }

                }
            }

            return balancesMap
        } catch (ex: Exception) {
            return null
        }
    }

    private fun getDataParam(event: String): JsonElement? {
        val params = event.toJsonObject()?.getAsJsonArray(PARAMS_KEY) ?: return null

        if (params.size() == 0) {
            return null
        }

        return params[1].asJsonArray
    }

    private fun configureGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(ContractBalance::class.java, ContractBalance.Deserializer())
            .create()
    }

    companion object {
        private const val PARAMS_KEY = "params"
        private val LOGGER =
            LoggerCoreComponent.create(ContractSubscriptionManagerImpl::class.java.name)
    }


}
