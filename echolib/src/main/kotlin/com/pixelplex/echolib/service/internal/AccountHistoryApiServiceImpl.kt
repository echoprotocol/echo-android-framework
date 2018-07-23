package com.pixelplex.echolib.service.internal

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.ILLEGAL_ID
import com.pixelplex.echolib.core.crypto.CryptoCoreComponent
import com.pixelplex.echolib.core.socket.SocketCoreComponent
import com.pixelplex.echolib.model.HistoryResponse
import com.pixelplex.echolib.model.network.Network
import com.pixelplex.echolib.model.socketoperations.GetAccountHistorySocketOperation
import com.pixelplex.echolib.service.AccountHistoryApiService

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
