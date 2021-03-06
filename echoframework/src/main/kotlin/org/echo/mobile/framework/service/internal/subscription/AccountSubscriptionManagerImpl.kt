package org.echo.mobile.framework.service.internal.subscription

import com.google.gson.JsonArray
import org.echo.mobile.framework.AccountListener
import org.echo.mobile.framework.model.FullAccount
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.service.AccountSubscriptionManager
import org.echo.mobile.framework.support.toJsonObject
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of [AccountSubscriptionManager]
 *
 * @author Dmitriy Bushuev
 */
class AccountSubscriptionManagerImpl(private val network: Network) :
    AccountSubscriptionManager {

    private val listeners = ConcurrentHashMap<String, MutableList<AccountListener>>()

    override fun registerListener(id: String, listener: AccountListener) {
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

    override fun removeListeners(id: String): MutableList<AccountListener>? = listeners.remove(id)

    override fun clear() = listeners.clear()

    override fun notify(account: FullAccount) {
        val objectId = account.account!!.getObjectId()
        listeners[objectId]?.forEach { listener ->
            listener.onChange(account)
        }
    }

    /**
     * Searches for account object id that has active subscriptions
     *
     *  Steps:
     *      1) Find not empty array with depth = 3 in params array.
     *         To receive result all events should be in example's form
     *      2) Go through all elements of array and find account statistic object with id starting from 2.6
     *         (@see http://docs.bitshares.org/development/blockchain/objects.html)
     *      3) Parse from statistic object account id with key "owner"
     *      4) Check, whether there are listeners for this account id.
     *         If true - return found account id, else - null
     *      5) Return null if any of steps fails
     */
    @SuppressWarnings("ReturnCount")
    override fun processEvent(event: String): List<String> {
        try {
            val params = event.toJsonObject()?.getAsJsonArray(PARAMS_KEY) ?: return listOf()

            if (params.size() == 0) {
                return listOf()
            }

            val firstParam = params[1].asJsonArray

            if (firstParam.size() == 0) {
                return listOf()
            }

            val statisticArray = firstParam[0].asJsonArray

            if (statisticArray.size() == 0) {
                return listOf()
            }

            return parseIdFromStatistic(statisticArray)

        } catch (ex: Exception) {
            return emptyList()
        }
    }

    private fun parseIdFromStatistic(statisticArray: JsonArray): List<String> {
        var accountId: String
        val ids = mutableListOf<String>()

        for (i in 0..(statisticArray.size() - 1)) {
            val statisticObject = statisticArray[i].asJsonObject

            val objectId = statisticObject?.get(OBJECT_ID_KEY)?.asString ?: continue

            if (!objectId.startsWith(ACCOUNT_STATISTIC_OBJECT_ID)) continue

            accountId = statisticObject.get(ACCOUNT_OWNER_KEY)?.asString ?: continue

            if (listeners[accountId] == null) {
                continue
            } else {
                ids.add(accountId)
            }
        }

        return ids
    }

    companion object {
        private const val PARAMS_KEY = "params"
        private const val OBJECT_ID_KEY = "id"
        private const val ACCOUNT_STATISTIC_OBJECT_ID = "2.5"
        private const val ACCOUNT_OWNER_KEY = "owner"
    }

}
