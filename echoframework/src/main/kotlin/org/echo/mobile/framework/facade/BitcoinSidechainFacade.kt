package org.echo.mobile.framework.facade

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.BtcAddress
import org.echo.mobile.framework.model.TransactionResult
import org.echo.mobile.framework.model.socketoperations.ResultCallback

/**
 * Encapsulates logic, associated with bitcoin sidechain functionality
 *
 * @author Dmitriy Bushuev
 */
interface BitcoinSidechainFacade {

    /**
     * Generates bitcoin address for required account [accountNameOrId]
     *
     * @param broadcastCallback     Callback for result of operation broadcast
     * @param resultCallback        Callback for retrieving result of operation (not required).
     *                              Retrieves result of transactions if exists -
     *                              history id which contains call contract result,
     *                              if not exists - empty string
     */
    fun generateBitcoinAddress(
        accountNameOrId: String,
        wif: String,
        backupAddress: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: ResultCallback<TransactionResult>
    )

    /**
     * Retrieves [BtcAddress] list for required account [accountNameOrId]
     */
    fun getBitcoinAddress(
        accountNameOrId: String,
        callback: Callback<BtcAddress>
    )

    /**
     * Transfers eth [value] from account [accountNameOrId]
     * to btc address [btcAddress] using [feeAsset] as fee currency
     */
    fun btcWithdraw(
        accountNameOrId: String,
        wif: String,
        btcAddress: String,
        value: String,
        feeAsset: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: ResultCallback<TransactionResult>
    )

}