package com.pixelplex.echolib.service.internal

import com.pixelplex.echolib.AccountListener
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.ILLEGAL_ID
import com.pixelplex.echolib.core.socket.SocketCoreComponent
import com.pixelplex.echolib.core.socket.SocketMessengerListener
import com.pixelplex.echolib.exception.LocalException
import com.pixelplex.echolib.model.*
import com.pixelplex.echolib.model.network.Network
import com.pixelplex.echolib.model.socketoperations.*
import com.pixelplex.echolib.service.DatabaseApiService
import com.pixelplex.echolib.support.EmptyCallback
import com.pixelplex.echolib.support.Result
import com.pixelplex.echolib.support.concurrent.future.FutureTask
import com.pixelplex.echolib.support.concurrent.future.wrapResult
import com.pixelplex.echolib.support.fold
import com.pixelplex.echolib.support.toJsonObject
import java.util.concurrent.TimeUnit

/**
 * Implementation of [DatabaseApiService]
 *
 * <p>
 *     Encapsulates logic of preparing API calls to [SocketCode]
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class DatabaseApiServiceImpl(
    private val socketCoreComponent: SocketCoreComponent,
    private val network: Network
) : DatabaseApiService {

    override var id: Int = ILLEGAL_ID

    private var socketMessengerListener: SocketMessengerListener = SubscriptionListener()

    @Volatile
    private var subscribed: Boolean = false

    private val subscriptionManager: AccountSubscriptionManager by lazy {
        AccountSubscriptionManagerImpl()
    }

    override fun getFullAccounts(
        namesOrIds: List<String>,
        subscribe: Boolean,
        callback: Callback<Map<String, FullAccount>>
    ) {
        val fullAccountsOperation = FullAccountsSocketOperation(
            id,
            namesOrIds,
            subscribe,
            callback = callback,
            network = network
        )
        socketCoreComponent.emit(fullAccountsOperation)
    }

    override fun getFullAccounts(
        namesOrIds: List<String>,
        subscribe: Boolean
    ): Result<Exception, Map<String, FullAccount>> {

        val future = FutureTask<Map<String, FullAccount>>()
        val fullAccountsOperation = FullAccountsSocketOperation(
            id,
            namesOrIds,
            subscribe,
            callback = object : Callback<Map<String, FullAccount>> {
                override fun onSuccess(result: Map<String, FullAccount>) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            },
            network = network
        )
        socketCoreComponent.emit(fullAccountsOperation)

        return future.wrapResult(mapOf())
    }

    override fun getChainId(): Result<Exception, String> {
        val future = FutureTask<String>()
        val chainIdOperation = GetChainIdSocketOperation(
            id,
            callback = object : Callback<String> {
                override fun onSuccess(result: String) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            }
        )
        socketCoreComponent.emit(chainIdOperation)

        return future.wrapResult()
    }

    override fun getBlockData(): BlockData {
        val globalPropertiesResult = getDynamicGlobalProperties()
        val dynamicProperties = if (globalPropertiesResult is Result.Value) {
            globalPropertiesResult.value
        } else {
            throw (globalPropertiesResult as Result.Error).error
        }
        val expirationTime = TimeUnit.MILLISECONDS.toSeconds(dynamicProperties.date!!.time) +
                Transaction.DEFAULT_EXPIRATION_TIME
        val headBlockId = dynamicProperties.headBlockId
        val headBlockNumber = dynamicProperties.headBlockNumber
        return BlockData(headBlockNumber, headBlockId, expirationTime)
    }

    override fun getDynamicGlobalProperties(): Result<Exception, DynamicGlobalProperties> {
        val future = FutureTask<DynamicGlobalProperties>()
        val blockDataOperation = BlockDataSocketOperation(
            id,
            callback = object : Callback<DynamicGlobalProperties> {
                override fun onSuccess(result: DynamicGlobalProperties) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            }
        )
        socketCoreComponent.emit(blockDataOperation)

        return future.wrapResult()
    }

    override fun getRequiredFees(
        operations: List<BaseOperation>,
        asset: Asset
    ): Result<Exception, List<AssetAmount>> {
        val future = FutureTask<List<AssetAmount>>()
        val requiredFeesOperation = RequiredFeesSocketOperation(
            id,
            operations,
            asset,
            callback = object : Callback<List<AssetAmount>> {
                override fun onSuccess(result: List<AssetAmount>) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            }
        )
        socketCoreComponent.emit(requiredFeesOperation)

        return future.wrapResult()
    }

    override fun subscribeOnAccount(
        id: String,
        listener: AccountListener
    ) {
        synchronized(this) {
            if (!subscribed) {
                socketCoreComponent.on(socketMessengerListener)

                this.subscribed = subscribeCallBlocking()
            }

            if (subscriptionManager.registerListener(id, listener)) {
                getFullAccounts(listOf(id), true, EmptyCallback())
            }
        }
    }

    override fun unsubscribeFromAccount(id: String, callback: Callback<Boolean>) {
        synchronized(this) {
            val accountListeners = subscriptionManager.removeListeners(id)

            accountListeners?.let {
                callback.onSuccess(true)
            } ?: callback.onError(LocalException("No listeners found for this account"))
        }
    }

    override fun unsubscribeAll(callback: Callback<Boolean>) {
        val unsubscribeOperation = createSubscriptionOperation(true, object : Callback<Any> {
            override fun onSuccess(result: Any) {
                subscriptionManager.clear()
                callback.onSuccess(true)
            }

            override fun onError(error: LocalException) {
                callback.onError(error)
            }

        })

        socketCoreComponent.emit(unsubscribeOperation)
    }

    private fun subscribeCallBlocking(): Boolean {
        val futureResult = FutureTask<Boolean>()

        val subscriptionOperation = createSubscriptionOperation(true, object : Callback<Any> {
            override fun onSuccess(result: Any) {
                futureResult.setComplete(true)
            }

            override fun onError(error: LocalException) {
                futureResult.setComplete(error)
            }

        })

        socketCoreComponent.emit(subscriptionOperation)

        var result = false

        futureResult.wrapResult<Exception, Boolean>(false).fold({
            result = it
        }, {
            result = false
        })

        return result
    }

    private fun createSubscriptionOperation(clearFilter: Boolean, callback: Callback<Any>) =
        SetSubscribeCallbackSocketOperation(
            id,
            clearFilter,
            SocketMethodType.CALL,
            callback
        )

    private inner class SubscriptionListener : SocketMessengerListener {

        override fun onEvent(event: String) {
            val jsonObject = event.toJsonObject()
            // no need to process other events)
            if (jsonObject.get(DatabaseApiServiceImpl.METHOD_KEY).asString !=
                DatabaseApiServiceImpl.NOTICE_METHOD_KEY
            ) {
                return
            }

            val accountId = subscriptionManager.processEvent(event) ?: return

            getFullAccounts(
                listOf(accountId),
                false,
                FullAccountSubscriptionCallback(accountId)
            )
        }

        // implement failures
        override fun onFailure(error: Throwable) {
        }

        override fun onConnected() {
        }

        override fun onDisconnected() {
        }

        private inner class FullAccountSubscriptionCallback(private val accountId: String) :
            Callback<Map<String, FullAccount>> {
            override fun onSuccess(result: Map<String, FullAccount>) {
                val account = result[accountId]?.account ?: return

                subscriptionManager.notify(account)
            }

            override fun onError(error: LocalException) {
            }

        }

    }

    companion object {
        const val METHOD_KEY = "method"
        private const val NOTICE_METHOD_KEY = "notice"
    }
}


