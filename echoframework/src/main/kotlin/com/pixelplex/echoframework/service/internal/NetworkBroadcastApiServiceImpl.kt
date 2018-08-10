package com.pixelplex.echoframework.service.internal

import com.pixelplex.echoframework.ILLEGAL_ID
import com.pixelplex.echoframework.core.crypto.CryptoCoreComponent
import com.pixelplex.echoframework.core.socket.SocketCoreComponent
import com.pixelplex.echoframework.model.Transaction
import com.pixelplex.echoframework.model.socketoperations.TransactionSocketOperation
import com.pixelplex.echoframework.service.NetworkBroadcastApiService
import com.pixelplex.echoframework.support.Result
import com.pixelplex.echoframework.support.concurrent.future.FutureTask
import com.pixelplex.echoframework.support.concurrent.future.completeCallback
import com.pixelplex.echoframework.support.concurrent.future.wrapResult

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
