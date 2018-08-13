package com.pixelplex.echoframework.service.internal

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.pixelplex.echoframework.AccountListener
import com.pixelplex.echoframework.model.Account
import com.pixelplex.echoframework.model.AccountOptions
import com.pixelplex.echoframework.model.Authority
import com.pixelplex.echoframework.model.network.Network
import com.pixelplex.echoframework.service.AccountSubscriptionManager
import com.pixelplex.echoframework.support.toJsonObject
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of [AccountSubscriptionManager]
 *
 * @author Dmitriy Bushuev
 */
class AccountSubscriptionManagerImpl(private val network: Network) :
    AccountSubscriptionManager {

    private val listeners = ConcurrentHashMap<String, MutableList<AccountListener>>()

    private val gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(Authority::class.java, Authority.Deserializer(network))
            .registerTypeAdapter(Account::class.java, Account.Deserializer())
            .registerTypeAdapter(
                AccountOptions::class.java,
                AccountOptions.Deserializer(network)
            )
            .create()
    }

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
     * Searches for account object that has active subscriptions and notifies all listeners
     */
    @SuppressWarnings("ReturnCount")
    override fun processEvent(event: String) {
        val params = event.toJsonObject()?.getAsJsonArray(PARAMS_KEY) ?: return

        if (params.size() == 0) {
            return
        }

        val firstParam = params[1].asJsonArray

        if (firstParam.size() == 0) {
            return
        }

        val notifyEventObjects = firstParam[0].asJsonArray

        if (notifyEventObjects.size() == 0) {
            return
        }

        parseAccount(notifyEventObjects)
    }

    private fun parseAccount(statisticArray: JsonArray) {
        for (i in 0..(statisticArray.size() - 1)) {
            val statisticObject = statisticArray[i].asJsonObject

            val objectId = statisticObject?.get(OBJECT_ID_KEY)?.asString ?: continue

            if (!objectId.startsWith(ACCOUNT_OBJECT_ID) || !listeners.keys.contains(objectId)) continue

            gson.fromJson<Account>(statisticObject, Account::class.java)?.let { notify(it) }
        }
    }

    companion object {
        private const val PARAMS_KEY = "params"
        private const val OBJECT_ID_KEY = "id"
        private const val ACCOUNT_OBJECT_ID = "1.2"
    }

}
