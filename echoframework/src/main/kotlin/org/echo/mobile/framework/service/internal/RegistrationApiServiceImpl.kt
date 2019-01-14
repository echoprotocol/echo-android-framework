package org.echo.mobile.framework.service.internal

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.ILLEGAL_ID
import org.echo.mobile.framework.core.socket.SocketCoreComponent
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.exception.RegistrationException
import org.echo.mobile.framework.model.FullAccount
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.model.socketoperations.RegisterSocketOperation
import org.echo.mobile.framework.service.RegistrationApiService

/**
 * Implementation of [RegistrationApiService]
 *
 * Encapsulates logic of preparing API calls to [SocketCoreComponent]
 *
 * @author Daria Pechkovskaya
 */
class RegistrationApiServiceImpl(
    private val socketCoreComponent: SocketCoreComponent,
    private val network: Network
) : RegistrationApiService {

    override var id: Int = ILLEGAL_ID

    override fun register(
        accountName: String,
        keyOwner: String,
        keyActive: String,
        keyMemo: String,
        echorandKey: String,
        callback: Callback<FullAccount>
    ) {

        val fullAccountsOperation = RegisterSocketOperation(
            id,
            accountName,
            keyOwner,
            keyActive,
            keyMemo,
            echorandKey,
            callId = socketCoreComponent.currentId,
            callback = object : Callback<FullAccount> {

                override fun onSuccess(result: FullAccount) {
                    callback.onSuccess(result)
                }

                override fun onError(error: LocalException) {
                    callback.onError(RegistrationException("Can't register account", error))
                }

            },
            network = network
        )
        socketCoreComponent.emit(fullAccountsOperation)
    }
}
