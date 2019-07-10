package org.echo.mobile.framework.facade

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.EthAddress
import org.echo.mobile.framework.model.EthDeposit
import org.echo.mobile.framework.model.EthWithdraw
import org.echo.mobile.framework.model.TransactionResult

/**
 * Encapsulates logic, associated with sidechain functionality
 *
 * @author Dmitriy Bushuev
 */
interface SidechainFacade {

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
     * to eth address [ethAddress] using [feeAsset] asfee currency
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

    /**
     * Retrieves list of account's [accountNameOrId] deposits [EthDeposit]
     */
    fun getAccountDeposits(accountNameOrId: String, callback: Callback<List<EthDeposit>>)

    /**
     * Retrieves list of account's [accountNameOrId] withdrawals [EthWithdraw]
     */
    fun getAccountWithdrawals(accountNameOrId: String, callback: Callback<List<EthWithdraw>>)

}