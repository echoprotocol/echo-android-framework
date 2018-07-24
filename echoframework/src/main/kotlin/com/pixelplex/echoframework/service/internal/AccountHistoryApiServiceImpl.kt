package com.pixelplex.echoframework.service.internal

import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.ILLEGAL_ID
import com.pixelplex.echoframework.core.crypto.CryptoCoreComponent
import com.pixelplex.echoframework.core.socket.SocketCoreComponent
import com.pixelplex.echoframework.model.HistoryResponse
import com.pixelplex.echoframework.model.network.Network
import com.pixelplex.echoframework.model.socketoperations.GetAccountHistorySocketOperation
import com.pixelplex.echoframework.service.AccountHistoryApiService

/**
 * Implementation of [AccountHistoryApiService]
 *
 * <p>
 *     Encapsulates logic of preparing API calls to [SocketCode]
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class AccountHistoryApiServiceImpl(
    private val socketCoreComponent: SocketCoreComponent,
    private val cryptoCoreComponent: CryptoCoreComponent,
    private val network: Network
) : AccountHistoryApiService {

    override var id: Int = ILLEGAL_ID

    override fun getAccountHistory(
        accountId: String, start: String, stop: String, limit: Int,
        callback: Callback<HistoryResponse>
    ) {
        val historyCall = GetAccountHistorySocketOperation(
            id, accountId, start, stop, limit, network, callback = callback
        )

        socketCoreComponent.emit(historyCall)
    }

}
