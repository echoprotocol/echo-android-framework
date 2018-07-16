package com.pixelplex.echolib.service.internal

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.core.socket.SocketCoreComponent
import com.pixelplex.echolib.model.FullUserAccount
import com.pixelplex.echolib.model.socketoperations.FullAccountsSocketOperation
import com.pixelplex.echolib.service.DatabaseApiService
import com.pixelplex.echolib.support.Api

/**
 * Implementation of [DatabaseApiService]
 *
 * <p>
 *     Encapsulates logic of preparing API calls to [SocketCode]
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class DatabaseApiServiceImpl(private val socketCoreComponent: SocketCoreComponent) :
    DatabaseApiService {

    override val api: Api = Api.DATABASE

    override fun getFullAccounts(
        namesOrIds: List<String>,
        subscribe: Boolean,
        callback: Callback<List<FullUserAccount>>
    ) {
        val fullAccountsOperation = FullAccountsSocketOperation(
            api,
            namesOrIds,
            subscribe,
            callback = callback
        )
        socketCoreComponent.emit(fullAccountsOperation)
    }
}
