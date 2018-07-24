package com.pixelplex.echoframework.service

import com.pixelplex.echoframework.model.Transaction
import com.pixelplex.echoframework.support.Result

/**
 * Encapsulates logic, associated with blockchain network broadcast API
 *
 * <p>
 *     Graphene blockchain network broadcast API:
 *     http://docs.bitshares.org/api/network_broadcast.html
 * </p>
 *
 * @author Dmitriy Bushuev
 */
interface NetworkBroadcastApiService : ApiService, TransactionsService

/**
 * Encapsulates logic, associated with transactions
 */
interface TransactionsService {

    /**
     * Broadcast a [transaction]  to the network.
     *
     * @param transaction Transaction to broadcast
     */
    fun broadcastTransactionWithCallback(transaction: Transaction): Result<Exception, String>

}
