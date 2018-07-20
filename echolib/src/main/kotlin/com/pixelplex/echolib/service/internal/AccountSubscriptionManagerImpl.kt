package com.pixelplex.echolib.service.internal

import com.pixelplex.echolib.AccountListener
import com.pixelplex.echolib.model.Account
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of [AccountSubscriptionManager]
 *
 * @author Dmitriy Bushuev
 */
class AccountSubscriptionManagerImpl : AccountSubscriptionManager {

    private val listeners = ConcurrentHashMap<String, MutableList<AccountListener>>()

    override fun registerListener(id: String, listener: AccountListener): Boolean {
        var needAccountRequest = false

        val accountListeners = listeners[id]

        if (accountListeners == null) {
            val listenersByName = mutableListOf(listener)
            listeners[id] = listenersByName
            needAccountRequest = true
        } else {
            accountListeners += listener
        }

        return needAccountRequest
    }

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
     *         To receive result all events should be in example form
     *      2) Go through all elements of array and find account statistic object whit id starting from 2.6
     *         (@see http://docs.bitshares.org/development/blockchain/objects.html)
     *      3) Parse from statistic object account id with key "owner"
     *      4) Check, whether there are listeners for this account id.
     *         If true - return found account id, else - null
     *      5) Return null if any of steps fails
     */
    @SuppressWarnings("ReturnCount")
    override fun processEvent(event: String): String? {
        val params = JSONObject(event).getJSONArray(PARAMS_KEY)

        if (params.length() == 0) {
            return null
        }

        val firstParam = params.getJSONArray(1)

        if (firstParam.length() == 0) {
            return null
        }

        val statisticArray = firstParam.getJSONArray(0)

        if (statisticArray.length() == 0) {
            return null
        }

        return parseIdFromStatistic(statisticArray)
    }

    private fun parseIdFromStatistic(statisticArray: JSONArray): String? {
        var accountId: String? = null

        for (i in 0..(statisticArray.length() - 1)) {
            val statisticObject = statisticArray.getJSONObject(i)

            val objectId = statisticObject?.getString(OBJECT_ID_KEY) ?: continue

            if (!objectId.startsWith(ACCOUNT_STATISTIC_OBJECT_ID)) continue

            accountId = statisticObject.getString(ACCOUNT_OWNER_KEY) ?: continue

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