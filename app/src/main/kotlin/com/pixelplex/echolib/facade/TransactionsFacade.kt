package com.pixelplex.echolib.facade

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.model.HistoryResponse

/**
 * Encapsulates logic, associated with various blockchain transactions processes
 *
 * @author Dmitriy Bushuev
 */
interface TransactionsFacade {

    /**
     * Describes transfer operation logic contract
     *
     * @param nameOrId   Source account name or id
     * @param password   Source account password for retrieving access to funds
     * @param toNameOrId Transfer target account name or id
     * @param amount     Value amount of transfer operation
     * @param asset      Specific asset type id
     * @param callback   Listener of operation results.
     *                   Retrieves transfer operation id if succeed
     */
    fun sendTransferOperation(
        nameOrId: String,
        password: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        callback: Callback<String>
    )

    /**
     * Describes account history retrieving logic contract
     *
     * @param nameOrId             Source account name or id
     * @param transactionStartId   ID of the most recent operation to retrieve
     * @param transactionStopId    ID of the earliest operation to retrieve
     * @param limit                Maximum number of operations to retrieve
     * @param asset                Specific asset type id
     * @param callback             Listener of operation results
     */
    fun getAccountHistory(
        nameOrId: String,
        transactionStartId: String,
        transactionStopId: String,
        limit: Int,
        asset: String,
        callback: Callback<HistoryResponse>
    )

}
