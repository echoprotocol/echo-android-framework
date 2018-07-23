package com.pixelplex.echolib.facade.internal

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.facade.TransactionsFacade
import com.pixelplex.echolib.model.HistoryResponse
import com.pixelplex.echolib.service.AccountHistoryApiService
import com.pixelplex.echolib.service.NetworkBroadcastApiService

/**
 * Implementation of [TransactionsFacade]
 *
 * <p>
 *     Delegates API call logic to [NetworkBroadcastApiService] and [AccountHistoryApiService]
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class TransactionsFacadeImpl(
    private val networkBroadcastApiService: NetworkBroadcastApiService,
    private val accountHistoryApiService: AccountHistoryApiService
) : TransactionsFacade {

    override fun sendTransferOperation(
        nameOrId: String,
        password: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        callback: Callback<String>
    ) {
    }

    override fun getAccountHistory(
        nameOrId: String,
        transactionStartId: String,
        transactionStopId: String,
        limit: Int,
        asset: String,
        callback: Callback<HistoryResponse>
    ) {
        accountHistoryApiService.getAccountHistory(
            nameOrId,
            transactionStartId,
            transactionStopId,
            limit,
            callback
        )
    }

}
