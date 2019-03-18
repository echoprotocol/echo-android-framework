package org.echo.mobile.framework.service

import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.support.Result

/**
 * Encapsulates logic, associated with blockchain network broadcast API
 *
 * [Graphene blockchain network broadcast API](https://dev-doc.myecho.app/classgraphene_1_1app_1_1network__broadcast__api.html)
 *
 * @author Dmitriy Bushuev
 */
interface NetworkBroadcastApiService : ApiService, TransactionsService, CustomOperationService

/**
 * Encapsulates logic, associated with transactions
 */
interface TransactionsService {

    /**
     * Broadcast a [transaction]  to the network.
     *
     * @param transaction Transaction to broadcast
     */
    fun broadcastTransaction(transaction: Transaction): Result<Exception, Boolean>

    /**
     * Broadcast a [transaction] to the network with callback from blockchain.
     *
     * @param transaction Transaction to broadcast.
     * @return Id of call to network
     */
    fun broadcastTransactionWithCallback(transaction: Transaction): Result<Exception, Int>

}
