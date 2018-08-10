package com.pixelplex.echoframework.service.internal

import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.ILLEGAL_ID
import com.pixelplex.echoframework.core.logger.internal.LoggerCoreComponent
import com.pixelplex.echoframework.core.socket.SocketCoreComponent
import com.pixelplex.echoframework.model.HistoryResponse
import com.pixelplex.echoframework.model.network.Network
import com.pixelplex.echoframework.model.socketoperations.GetAccountHistorySocketOperation
import com.pixelplex.echoframework.service.AccountHistoryApiService
import com.pixelplex.echoframework.support.Result
import com.pixelplex.echoframework.support.concurrent.future.FutureTask
import com.pixelplex.echoframework.support.concurrent.future.completeCallback
import com.pixelplex.echoframework.support.concurrent.future.wrapResult

/**
 * Implementation of [AccountHistoryApiService]
 *
 * Encapsulates logic of preparing API calls to [SocketCoreComponent]
 *
 * @author Dmitriy Bushuev
 */
class AccountHistoryApiServiceImpl(
    private val socketCoreComponent: SocketCoreComponent,
    private val network: Network
) : AccountHistoryApiService {

    override var id: Int = ILLEGAL_ID

    override fun getAccountHistory(
        accountId: String, start: String, stop: String, limit: Int,
        callback: Callback<HistoryResponse>
    ) {
        val historyCall = GetAccountHistorySocketOperation(
            id,
            accountId,
            start,
            stop,
            limit,
            network,
            callId = socketCoreComponent.currentId,
            callback = callback
        )

        socketCoreComponent.emit(historyCall)
    }

    override fun getAccountHistory(
        accountId: String,
        start: String,
        stop: String,
        limit: Int
    ): Result<Exception, HistoryResponse> {
        val historyFuture = FutureTask<HistoryResponse>()
        getAccountHistory(
            accountId,
            start,
            stop,
            limit,
            historyFuture.completeCallback(successBlock = { result ->
                result.takeIf { it.transactions.isEmpty() }
                    ?.let {
                        LOGGER.log(
                            "Empty history received for account $accountId " +
                                    "with start = $start and stop = $stop"
                        )
                    }
            })
        )

        return historyFuture.wrapResult(HistoryResponse(listOf()))
    }

    companion object {
        private val LOGGER =
            LoggerCoreComponent.create(AccountHistoryApiServiceImpl::class.java.name)
    }

}
