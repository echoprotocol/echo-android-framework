package org.echo.mobile.framework.facade

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.DEFAULT_GAS_LIMIT
import org.echo.mobile.framework.DEFAULT_GAS_PRICE
import org.echo.mobile.framework.model.contract.input.InputValue

/**
 * Encapsulates logic, associated with fee configuration processes
 *
 * @author Dmitriy Bushuev
 */
interface FeeFacade {

    /**
     * Counts required fee for defined transaction settings
     *
     * @param fromNameOrId  Source account name or id
     * @param password      User account password for memo encryption
     * @param toNameOrId    Target account name or id
     * @param amount        Amount value of transfer
     * @param asset         Specific asset type id
     * @param feeAsset      Asset for fee calculating
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
     * Counts required fee for defined operation settings
     *
     * @param userNameOrId  Source account name or id
     * @param contractId    Id of contract for method calling
     * @param methodName    Name of contract method for calling
     * @param methodParams  Params of contract method for calling
     * @param assetId       Specific asset type id
     * @param feeAsset      Asset for fee calculating
     * @param callback      Listener of operation results
     */
    fun getFeeForContractOperation(
        userNameOrId: String,
        contractId: String,
        methodName: String,
        methodParams: List<InputValue>,
        assetId: String,
        feeAsset: String?,
        callback: Callback<String>
    )

}
