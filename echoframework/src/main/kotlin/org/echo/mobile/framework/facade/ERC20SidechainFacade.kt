package org.echo.mobile.framework.facade

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.ERC20Deposit
import org.echo.mobile.framework.model.ERC20Token
import org.echo.mobile.framework.model.ERC20Withdrawal
import org.echo.mobile.framework.model.TransactionResult
import org.echo.mobile.framework.model.socketoperations.ResultCallback

/**
 * Encapsulates logic, associated with erc20 sidechain functionality
 *
 * @author Dmitriy Bushuev
 */
interface ERC20SidechainFacade {

    /**
     * Registers [ethAddress] token with [name], [symbol] and [decimals] in ECHO network
     */
    fun registerERC20Token(
        accountNameOrId: String,
        wif: String,
        ethAddress: String,
        name: String,
        symbol: String,
        decimals: String,
        feeAsset: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: ResultCallback<TransactionResult>
    )

    /**
     * Transfers erc20 token [value] from account [accountNameOrId]
     * to eth address [ethAddress] using [feeAsset] as fee currency
     */
    fun erc20Withdraw(
        accountNameOrId: String,
        wif: String,
        ethAddress: String,
        ethTokenId: String,
        value: String,
        feeAsset: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: ResultCallback<TransactionResult>
    )

    /**
     * Retrieves corresponding [ERC20Token] for required address [address]
     */
    fun getERC20TokenByAddress(
        address: String,
        callback: Callback<ERC20Token>
    )

    /**
     * Retrieves corresponding [ERC20Token] for required address [tokenId]
     */
    fun getERC20TokenByTokenId(
        tokenId: String,
        callback: Callback<ERC20Token>
    )

    /**
     * Retrieves corresponding [ERC20Token] for required address [contractId]
     */
    fun getERC20TokenByContractId(
            contractId: String,
        callback: Callback<ERC20Token>
    )

    /**
     * Checks whether [contractId] is ERC20 token
     */
    fun checkERC20Token(
        contractId: String,
        callback: Callback<Boolean>
    )

    /**
     * Retrieves erc20 deposits for [accountNameOrId]
     */
    fun getERC20AccountDeposits(accountNameOrId: String, callback: Callback<List<ERC20Deposit>>)

    /**
     * Retrieves erc20 withdrawals for [accountNameOrId]
     */
    fun getERC20AccountWithdrawals(
        accountNameOrId: String,
        callback: Callback<List<ERC20Withdrawal>>
    )

}