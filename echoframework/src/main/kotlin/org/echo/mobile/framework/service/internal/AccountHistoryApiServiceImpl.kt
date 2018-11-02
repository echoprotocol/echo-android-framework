package org.echo.mobile.framework.service.internal

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.ILLEGAL_ID
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.core.socket.SocketCoreComponent
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.HistoryResponse
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.model.socketoperations.CustomOperation
import org.echo.mobile.framework.model.socketoperations.CustomSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetAccountHistorySocketOperation
import org.echo.mobile.framework.service.AccountHistoryApiService
import org.echo.mobile.framework.support.Result
import org.echo.mobile.framework.support.concurrent.future.FutureTask
import org.echo.mobile.framework.support.concurrent.future.completeCallback
import org.echo.mobile.framework.support.concurrent.future.wrapResult

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

    companion object {
        private val LOGGER =
            LoggerCoreComponent.create(AccountHistoryApiServiceImpl::class.java.name)
    }

}
