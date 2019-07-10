package org.echo.mobile.framework.facade

import org.echo.mobile.framework.Callback

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
     * @param wif        Account's private key in wif format
     * @param toNameOrId Transfer target account name or id
     * @param amount     Value amount of transfer operation
     * @param asset      Specific asset type id
     * @param feeAsset   Specific asset type id for calculating fee. If null - [asset] will be used
     * @param callback   Listener of operation results.
     *                   Retrieves true if transfer succeed,  otherwise - false
     */
    fun sendTransferOperation(
        nameOrId: String,
        wif: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        feeAsset: String?,
        callback: Callback<Boolean>
    )

}
