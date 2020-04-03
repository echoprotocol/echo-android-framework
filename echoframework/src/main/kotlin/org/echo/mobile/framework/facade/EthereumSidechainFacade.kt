package org.echo.mobile.framework.facade

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.EthAddress
import org.echo.mobile.framework.model.TransactionResult

/**
 * Encapsulates logic, associated with ethereum sidechain functionality
 *
 * @author Dmitriy Bushuev
 */
interface EthereumSidechainFacade {

    /**
     * Generates ethereum address for required account [accountNameOrId]
     *
     * @param broadcastCallback     Callback for result of operation broadcast
     * @param resultCallback        Callback for retrieving result of operation (not required).
     *                              Retrieves result of transactions if exists -
     *                              history id which contains call contract result,
     *                              if not exists - empty string
     */
    fun generateEthereumAddress(
        accountNameOrId: String,
        wif: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<TransactionResult>?
    )

    /**
     * Transfers eth [value] from account [accountNameOrId]
     * to eth address [ethAddress] using [feeAsset] as fee currency
     */
    fun ethWithdraw(
        accountNameOrId: String,
        wif: String,
        ethAddress: String,
        value: String,
        feeAsset: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<TransactionResult>?
    )

    /**
     * Retrieves [EthAddress] list for required account [accountNameOrId]
     */
    fun getEthereumAddress(
        accountNameOrId: String,
        callback: Callback<EthAddress>
    )

}