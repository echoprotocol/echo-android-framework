package org.echo.mobile.framework.service.internal

import org.echo.mobile.framework.ILLEGAL_ID
import org.echo.mobile.framework.core.socket.SocketCoreComponent
import org.echo.mobile.framework.model.socketoperations.RegisterSocketOperation
import org.echo.mobile.framework.service.RegistrationApiService
import org.echo.mobile.framework.support.Result
import org.echo.mobile.framework.support.concurrent.future.FutureTask
import org.echo.mobile.framework.support.concurrent.future.completeCallback
import org.echo.mobile.framework.support.concurrent.future.wrapResult

/**
 * Implementation of [RegistrationApiService]
 *
 * Encapsulates logic of preparing API calls to [SocketCoreComponent]
 *
 * @author Daria Pechkovskaya
 */
class RegistrationApiServiceImpl(private val socketCoreComponent: SocketCoreComponent) :
    RegistrationApiService {

    override var id: Int = ILLEGAL_ID

    override fun register(
        accountName: String,
        keyActive: String,
        echorandKey: String
    ): Result<Exception, Int> {
        val future = FutureTask<Int>()

        val fullAccountsOperation = RegisterSocketOperation(
            id,
            accountName,
            keyActive,
            echorandKey,
            callId = socketCoreComponent.currentId,
            callback = future.completeCallback())
        socketCoreComponent.emit(fullAccountsOperation)

        return future.wrapResult()
    }
}
