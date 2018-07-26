package com.pixelplex.echoframework.service.internal

import com.google.gson.JsonArray
import com.pixelplex.echoframework.AccountListener
import com.pixelplex.echoframework.model.Account
import com.pixelplex.echoframework.support.toJsonObject
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of [AccountSubscriptionManager]
 *
 * @author Dmitriy Bushuev
 */
class AccountSubscriptionManagerImpl : AccountSubscriptionManager {

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

    override fun registered(id: String): Boolean = listeners.containsKey(id)

    override fun removeListeners(id: String): MutableList<AccountListener>? = listeners.remove(id)

    override fun clear() = listeners.clear()

    override fun notify(account: Account) {
        val objectId = account.getObjectId()
        listeners[objectId]?.forEach { listener ->
            listener.onChange(account)
        }
    }

    /**
     * Searches for account object id that has active subscriptions
     *
     * Example:
     * {
     *  "method":"notice",
     *  "params":
     *  [
     *      4,
     *       [
     *          [
     *              {
     *                  "id":"2.6.22620",
     *                  "owner":"1.2.22620"
     *              },
     *              {
     *                  "id":"1.6.68"
     *              },
     *              {
     *                  "id":"2.1.0"
     *              },
     *              {
     *                  "id":"2.6.23215",
     *                  "owner":"1.2.23215"
     *              },
     *              {
     *                  "id":"2.8.25373"
     *              },
     *              {
     *                  "id":"2.5.24777",
     *                  "owner":"1.2.23215"
     *              },
     *              {
     *                  "id":"2.5.23882",
     *                  "owner":"1.2.22620"
     *              },
     *              {
     *                  "id":"1.6.74",
     *                  "witness_account":"1.2.22574"
     *              }
     *          ]
     *       ]
     *  ]
     *  }
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
    override fun processEvent(event: String): String? {
        val params = event.toJsonObject()?.getAsJsonArray(PARAMS_KEY) ?: return null

        if (params.size() == 0) {
            return null
        }

        val firstParam = params[1].asJsonArray

        if (firstParam.size() == 0) {
            return null
        }

        val statisticArray = firstParam[0].asJsonArray

        if (statisticArray.size() == 0) {
            return null
        }

        return parseIdFromStatistic(statisticArray)
    }

    private fun parseIdFromStatistic(statisticArray: JsonArray): String? {
        var accountId: String? = null

        for (i in 0..(statisticArray.size() - 1)) {
            val statisticObject = statisticArray[i].asJsonObject

            val objectId = statisticObject?.get(OBJECT_ID_KEY)?.asString ?: continue

            if (!objectId.startsWith(ACCOUNT_STATISTIC_OBJECT_ID)) continue

            accountId = statisticObject.get(ACCOUNT_OWNER_KEY)?.asString ?: continue

            if (listeners[accountId] == null) {
                continue
            } else {
                break
            }
        }

        return accountId
    }

    companion object {
        private const val PARAMS_KEY = "params"
        private const val OBJECT_ID_KEY = "id"
        private const val ACCOUNT_STATISTIC_OBJECT_ID = "2.6"
        private const val ACCOUNT_OWNER_KEY = "owner"
    }

}
