package com.pixelplex.echolib.service.internal

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.core.socket.SocketCoreComponent
import com.pixelplex.echolib.exception.LocalException
import com.pixelplex.echolib.model.Account
import com.pixelplex.echolib.model.network.Network
import com.pixelplex.echolib.model.socketoperations.FullAccountsSocketOperation
import com.pixelplex.echolib.service.DatabaseApiService
import com.pixelplex.echolib.support.Api
import com.pixelplex.echolib.support.concurrent.future.FutureTask

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

    override val api: Api = Api.DATABASE

    override fun getFullAccounts(
        namesOrIds: List<String>,
        subscribe: Boolean,
        callback: Callback<Map<String, Account>>
    ) {
        val fullAccountsOperation = FullAccountsSocketOperation(
            api,
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
    ): Map<String, Account> {

        val future = FutureTask<Map<String, Account>>()
        val fullAccountsOperation = FullAccountsSocketOperation(
            api,
            namesOrIds,
            subscribe,
            callback = object : Callback<Map<String, Account>> {
                override fun onSuccess(result: Map<String, Account>) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            },
            network = network
        )
        socketCoreComponent.emit(fullAccountsOperation)

        return future.get() ?: mapOf()
    }
}
