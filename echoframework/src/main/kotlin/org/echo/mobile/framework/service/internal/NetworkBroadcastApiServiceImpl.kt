package org.echo.mobile.framework.service.internal

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.ILLEGAL_ID
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.core.socket.SocketCoreComponent
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.model.TransactionResult
import org.echo.mobile.framework.model.socketoperations.CustomOperation
import org.echo.mobile.framework.model.socketoperations.CustomSocketOperation
import org.echo.mobile.framework.model.socketoperations.TransactionSocketOperation
import org.echo.mobile.framework.model.socketoperations.TransactionWithCallbackSocketOperation
import org.echo.mobile.framework.service.NetworkBroadcastApiService
import org.echo.mobile.framework.support.Result
import org.echo.mobile.framework.support.concurrent.future.FutureTask
import org.echo.mobile.framework.support.concurrent.future.completeCallback
import org.echo.mobile.framework.support.concurrent.future.wrapResult

/**
 * Implementation of [NetworkBroadcastApiService]
 *
 * Encapsulates logic of preparing API calls to [SocketCoreComponent]
 *
 * @author Dmitriy Bushuev
 */
class NetworkBroadcastApiServiceImpl(
    private val socketCoreComponent: SocketCoreComponent,
    private val cryptoCoreComponent: CryptoCoreComponent
) : NetworkBroadcastApiService {

    override var id: Int = ILLEGAL_ID

    override fun broadcastTransaction(transaction: Transaction): Result<Exception, Boolean> {
        val future = FutureTask<Boolean>()
        val transactionSocketOperation = TransactionSocketOperation(
            id,
            transaction,
            cryptoCoreComponent.signTransaction(transaction),
            callId = socketCoreComponent.currentId,
            callback = future.completeCallback()
        )
        socketCoreComponent.emit(transactionSocketOperation)

        return future.wrapResult()
    }

    override fun broadcastTransactionWithCallback(transaction: Transaction): Result<Exception, Int> {
        val future = FutureTask<Int>()
        val transactionSocketOperation = TransactionWithCallbackSocketOperation(
            id,
            transaction,
            cryptoCoreComponent.signTransaction(transaction),
            callId = socketCoreComponent.currentId,
            callback = future.completeCallback()
        )
        socketCoreComponent.emit(transactionSocketOperation)

        return future.wrapResult()
    }

    override fun <T> callCustomOperation(operation: CustomOperation<T>, callback: Callback<T>) {
        val customSocketOperation = CustomSocketOperation(
            id,
            socketCoreComponent.currentId,
            operation,
            callback
        )
        socketCoreComponent.emit(customSocketOperation)
    }

    override fun <T> callCustomOperation(operation: CustomOperation<T>): Result<LocalException, T> {
        val futureTask = FutureTask<T>()
        val customSocketOperation = CustomSocketOperation(
            id,
            socketCoreComponent.currentId,
            operation,
            futureTask.completeCallback()
        )
        socketCoreComponent.emit(customSocketOperation)

        return futureTask.wrapResult()
    }
}
