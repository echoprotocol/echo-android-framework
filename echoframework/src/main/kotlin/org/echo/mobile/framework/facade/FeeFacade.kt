package org.echo.mobile.framework.facade

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.TransactionResult
import org.echo.mobile.framework.model.contract.ContractFee
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
     * @param wif           Account's private key in wif format
     * @param toNameOrId    Target account name or id
     * @param amount        Amount value of transfer
     * @param asset         Specific asset type id
     * @param feeAsset      Asset for fee calculating
     * @param callback      Listener of operation results
     */
    fun getFeeForTransferOperation(
        fromNameOrId: String,
        wif: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        feeAsset: String?,
        callback: Callback<String>
    )

    /**
     * Counts required fee for defined operation settings
     *
     * @param userNameOrId  Source account name or id
     * @param contractId    Id of contract for method calling
     * @param amount        Amount for contract call
     * @param methodName    Name of contract method for calling
     * @param methodParams  Params of contract method for calling
     * @param assetId       Specific asset type id
     * @param feeAsset      Asset for fee calculating
     * @param callback      Listener of operation results
     */
    fun getFeeForContractOperation(
        userNameOrId: String,
        contractId: String,
        amount: String,
        methodName: String,
        methodParams: List<InputValue>,
        assetId: String,
        feeAsset: String?,
        callback: Callback<ContractFee>
    )

    /**
     * Counts required fee for defined operation settings
     *
     * @param userNameOrId  Source account name or id
     * @param contractId    Id of contract for method calling
     * @param amount        Amount for contract call
     * @param code          Valid code for contract query
     * @param assetId       Specific asset type id
     * @param feeAsset      Asset for fee calculating
     * @param callback      Listener of operation results
     */
    fun getFeeForContractOperation(
        userNameOrId: String,
        contractId: String,
        amount: String,
        code: String,
        assetId: String,
        feeAsset: String?,
        callback: Callback<ContractFee>
    )

    /**
     * Counts required fee for defined contract creation operation settings
     *
     * @param userNameOrId  Source account name or id
     * @param amount        Amount for payable contract
     * @param byteCode      Valid contract bytecode
     * @param assetId       Specific asset type id
     * @param feeAsset      Asset for fee calculating
     * @param callback      Listener of operation results
     */
    fun getFeeForContractCreateOperation(
        userNameOrId: String,
        amount: String,
        byteCode: String,
        assetId: String,
        feeAsset: String?,
        callback: Callback<AssetAmount>
    )

    /**
     * Counts required fee for ethereum erc20 withdraw operation
     */
    fun getFeeForWithdrawErc20Operation(
            accountNameOrId: String,
            ethAddress: String,
            ethTokenId: String,
            value: String,
            feeAsset: String,
            callback: Callback<AssetAmount>
    )

    /**
     * Counts required fee for ethereum withdraw operation
     */
    fun getFeeForWithdrawEthereumOperation(
            accountNameOrId: String,
            ethAddress: String,
            value: String,
            feeAsset: String,
            callback: Callback<AssetAmount>
    )

    /**
     * Counts required fee for btc withdraw transaction
     */
    fun getFeeForWithdrawBtcOperation(
            accountNameOrId: String,
            btcAddress: String,
            value: String,
            feeAsset: String,
            callback: Callback<AssetAmount>
    )

}
