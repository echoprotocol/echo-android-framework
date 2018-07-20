package com.pixelplex.echolib.service.internal

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.ILLEGAL_ID
import com.pixelplex.echolib.core.socket.SocketCoreComponent
import com.pixelplex.echolib.exception.LocalException
import com.pixelplex.echolib.model.Transaction
import com.pixelplex.echolib.model.socketoperations.TransactionSocketOperation
import com.pixelplex.echolib.service.NetworkBroadcastApiService
import com.pixelplex.echolib.support.Result
import com.pixelplex.echolib.support.concurrent.future.FutureTask
import com.pixelplex.echolib.support.concurrent.future.wrapResult

/**
 * Implementation of [NetworkBroadcastApiService]
 *
 * <p>
 *     Encapsulates logic of preparing API calls to [SocketCode]
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class NetworkBroadcastApiServiceImpl(private val socketCoreComponent: SocketCoreComponent) :
    NetworkBroadcastApiService {

    override var id: Int = ILLEGAL_ID

    override fun broadcastTransactionWithCallback(transaction: Transaction): Result<Exception, String> {
        val future = FutureTask<String>()
        val transactionSocketOperation = TransactionSocketOperation(
            id,
            transaction,
            callback = object : Callback<String> {
                override fun onSuccess(result: String) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            }
        )
        socketCoreComponent.emit(transactionSocketOperation)

        return future.wrapResult()
    }
}
