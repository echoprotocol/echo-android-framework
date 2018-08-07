package com.pixelplex.echoframework.service

import com.pixelplex.echoframework.model.Transaction
import com.pixelplex.echoframework.support.Result

/**
 * Encapsulates logic, associated with blockchain network broadcast API
 *
 * [Graphene blockchain network broadcast API](https://dev-doc.myecho.app/classgraphene_1_1app_1_1network__broadcast__api.html)
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
    fun broadcastTransactionWithCallback(transaction: Transaction): Result<Exception, Boolean>

}
