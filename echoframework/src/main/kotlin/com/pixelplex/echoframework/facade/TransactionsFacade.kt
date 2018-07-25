package com.pixelplex.echoframework.facade

import com.pixelplex.echoframework.Callback

/**
 * Encapsulates logic, associated with various blockchain transactions processes
 *
 * @author Dmitriy Bushuev
 */
interface TransactionsFacade {

    /**
     * Submits transaction to blockchain
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

}
