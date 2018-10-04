package com.pixelplex.echoframework.facade

import com.pixelplex.echoframework.Callback

/**
 * Encapsulates logic, associated with fee configuration processes
 *
 * @author Dmitriy Bushuev
 */
interface FeeFacade {

    /**
     * Counts required fee for defined transaction settings with memo
     *
     * @param fromNameOrId  Source account name or id
     * @param password      User account password for memo encryption
     * @param toNameOrId    Target account name or id
     * @param amount        Amount value of transfer
     * @param asset         Specific asset type id
     * @param feeAsset      Asset for fee calculating
     * @param feeAsset      Memo of transfer operation
     * @param callback      Listener of operation results
     */
    fun getFeeForTransferOperation(
        fromNameOrId: String,
        password: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        feeAsset: String?,
        message: String?,
        callback: Callback<String>
    )

    /**
     * Counts required fee for defined transaction settings
     *
     * @param fromNameOrId  Source account name or id
     * @param toNameOrId    Target account name or id
     * @param amount        Amount value of transfer
     * @param asset         Specific asset type id
     * @param feeAsset      Asset for fee calculating
     * @param callback      Listener of operation results
     */
    fun getFeeForTransferOperation(
        fromNameOrId: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        feeAsset: String?,
        callback: Callback<String>
    )

}
