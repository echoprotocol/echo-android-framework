package org.echo.mobile.framework.service.internal

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.ILLEGAL_ID
import org.echo.mobile.framework.core.socket.SocketCoreComponent
import org.echo.mobile.framework.model.RegistrationTask
import org.echo.mobile.framework.model.socketoperations.RequestRegistrationTaskSocketOperation
import org.echo.mobile.framework.model.socketoperations.SubmitRegistrationSolutionSocketOperation
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

    override fun submitRegistrationSolution(
        accountName: String,
        keyActive: String,
        echorandKey: String,
        nonce: UnsignedLong,
        randNum: UnsignedLong
    ): Result<Exception, Int> {
        val future = FutureTask<Int>()

        val fullAccountsOperation = SubmitRegistrationSolutionSocketOperation(
            id,
            accountName,
            keyActive,
            echorandKey,
            nonce,
            randNum,
            callId = socketCoreComponent.currentId,
            callback = future.completeCallback()
        )
        socketCoreComponent.emit(fullAccountsOperation)

        return future.wrapResult()
    }

    override fun requestRegistrationTask(): Result<Exception, RegistrationTask> {
        val future = FutureTask<RegistrationTask>()

        val fullAccountsOperation = RequestRegistrationTaskSocketOperation(
            id, callId = socketCoreComponent.currentId,
            callback = future.completeCallback()
        )
        socketCoreComponent.emit(fullAccountsOperation)

        return future.wrapResult()
    }
}
