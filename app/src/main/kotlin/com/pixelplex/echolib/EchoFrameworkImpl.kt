package com.pixelplex.echolib

import com.pixelplex.echolib.core.socket.internal.SocketCoreComponentImpl
import com.pixelplex.echolib.facade.*
import com.pixelplex.echolib.facade.internal.*
import com.pixelplex.echolib.model.Account
import com.pixelplex.echolib.model.Balance
import com.pixelplex.echolib.model.HistoryResponse
import com.pixelplex.echolib.service.internal.AccountHistoryApiServiceImpl
import com.pixelplex.echolib.service.internal.DatabaseApiServiceImpl
import com.pixelplex.echolib.service.internal.NetworkBroadcastApiServiceImpl
import com.pixelplex.echolib.service.internal.NetworkNodesApiServiceImpl
import com.pixelplex.echolib.support.model.Settings

/**
 * Implementation of [EchoFramework] base library API
 *
 * <p>
 *     Delegates all logic to specific facades
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class EchoFrameworkImpl internal constructor(settings: Settings) : EchoFramework {

    private val authenticationFacade: AuthenticationFacade
    private val feeFacade: FeeFacade
    private val informationFacade: InformationFacade
    private val subscriptionFacade: SubscriptionFacade
    private val transactionsFacade: TransactionsFacade

    /**
     * Initializes and setups all facades with required dependencies
     */
    init {
        val socketCoreComponent = SocketCoreComponentImpl(settings.socketMessenger, settings.apis)

        val accountHistoryApiService =
            AccountHistoryApiServiceImpl(socketCoreComponent, settings.cryptoComponent)
        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent)
        val networkBroadcastApiService = NetworkBroadcastApiServiceImpl(socketCoreComponent)
        val networkNodesApiService = NetworkNodesApiServiceImpl(socketCoreComponent)

        authenticationFacade = AuthenticationFacadeImpl(accountHistoryApiService)
        feeFacade = FeeFacadeImpl(databaseApiService)
        informationFacade = InformationFacadeImpl(databaseApiService)
        subscriptionFacade =
                SubscriptionFacadeImpl(networkBroadcastApiService, networkNodesApiService)
        transactionsFacade =
                TransactionsFacadeImpl(networkBroadcastApiService, accountHistoryApiService)
    }

    override fun login(name: String, password: String, callback: Callback<Account>) =
        authenticationFacade.login(name, password, callback)

    override fun changePassword(
        nameOrId: String,
        oldPassword: String,
        newPassword: String,
        callback: Callback<Account>
    ) = authenticationFacade.changePassword(nameOrId, oldPassword, newPassword, callback)

    override fun getFeeForTransferOperation(
        fromNameOrId: String,
        toNameOrId: String,
        asset: String,
        callback: Callback<String>
    ) = feeFacade.getFeeForTransferOperation(fromNameOrId, toNameOrId, asset, callback)

    override fun getAccount(nameOrId: String, callback: Callback<Account>) =
        informationFacade.getAccount(nameOrId, callback)

    override fun checkAccountIsUnavailable(nameOrId: String, callback: Callback<Boolean>) =
        informationFacade.checkAccountIsUnavailable(nameOrId, callback)

    override fun getBalance(nameOrId: String, asset: String, callback: Callback<Balance>) =
        informationFacade.getBalance(nameOrId, asset, callback)

    override fun subscribeOnAccount(nameOrId: String, listener: AccountListener) =
        subscriptionFacade.subscribeOnAccount(nameOrId, listener)

    override fun unsubscribeFromAccount(nameOrId: String, callback: Callback<Boolean>) =
        subscriptionFacade.unsubscribeFromAccount(nameOrId, callback)

    override fun unsubscribeAll(callback: Callback<Boolean>) =
        subscriptionFacade.unsubscribeAll(callback)

    override fun sendTransferOperation(
        nameOrId: String,
        password: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        callback: Callback<String>
    ) = transactionsFacade.sendTransferOperation(
        nameOrId,
        password,
        toNameOrId,
        amount,
        asset,
        callback
    )

    override fun getAccountHistory(
        nameOrId: String,
        transactionStartId: String,
        transactionStopId: String,
        limit: Int,
        asset: String,
        callback: Callback<HistoryResponse>
    ) = transactionsFacade.getAccountHistory(
        nameOrId,
        transactionStartId,
        transactionStopId,
        limit,
        asset,
        callback
    )

}
