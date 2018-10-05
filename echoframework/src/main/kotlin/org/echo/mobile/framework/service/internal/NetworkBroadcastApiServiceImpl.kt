package org.echo.mobile.framework.service.internal

import org.echo.mobile.framework.ILLEGAL_ID
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.core.socket.SocketCoreComponent
import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.model.socketoperations.TransactionSocketOperation
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

    override fun broadcastTransactionWithCallback(transaction: Transaction): Result<Exception, Boolean> {
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
}
