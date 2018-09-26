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
     * @param feeAsset   Specific asset type id for calculating fee. If null - [asset] will be used
     * @param message    Additional payload to transfer operation
     * @param callback   Listener of operation results.
     *                   Retrieves true if transfer succeed,  otherwise - false
     */
    fun sendTransferOperation(
        nameOrId: String,
        password: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        feeAsset: String?,
        message: String?,
        callback: Callback<Boolean>
    )

}
