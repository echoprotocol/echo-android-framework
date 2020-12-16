package org.echo.mobile.framework.facade.internal

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.exception.AccountNotFoundException
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.facade.ERC20SidechainFacade
import org.echo.mobile.framework.model.*
import org.echo.mobile.framework.model.operations.SidechainERC20RegisterTokenOperation
import org.echo.mobile.framework.model.operations.WithdrawERC20Operation
import org.echo.mobile.framework.model.socketoperations.TransactionResultCallback
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.service.NetworkBroadcastApiService
import org.echo.mobile.framework.support.EthAddressValidator
import org.echo.mobile.framework.support.dematerialize
import org.echo.mobile.framework.support.fold

/**
 * Implementation of [ERC20SidechainFacade]
 *
 * @author Dmitriy Bushuev
 */
class ERC20SidechainFacadeImpl(
    private val databaseApiService: DatabaseApiService,
    private val networkBroadcastApiService: NetworkBroadcastApiService,
    private val cryptoCoreComponent: CryptoCoreComponent,
    private val transactionExpirationDelay: Long
) :
    BaseTransactionsFacade(databaseApiService, cryptoCoreComponent, transactionExpirationDelay),
    ERC20SidechainFacade {

    override fun registerERC20Token(
        accountNameOrId: String,
        wif: String,
        ethAddress: String,
        name: String,
        symbol: String,
        decimals: String,
        feeAsset: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: TransactionResultCallback
    ) {
        try {
            val account = findAccount(accountNameOrId)
            checkOwnerAccount(wif, account)
            val privateKey = cryptoCoreComponent.decodeFromWif(wif)

            val processedAddress =
                    ethAddress.replace(EthAddressValidator.ADDRESS_PREFIX, "").toLowerCase()

            val operation =
                    SidechainERC20RegisterTokenOperation(
                            Account(account.getObjectId()),
                            processedAddress,
                            name,
                            symbol,
                            UnsignedLong.valueOf(decimals)
                    )

            val transaction = configureTransaction(operation, privateKey, feeAsset)
            val broadcastTransaction = networkBroadcastApiService
                    .broadcastTransaction(transaction)

            broadcastCallback.onSuccess(true)
            resultCallback.processResult(broadcastTransaction)
        } catch (exception: Exception) {
            broadcastCallback.onError(exception as? LocalException ?: LocalException(exception))
            return
        }
    }

    override fun erc20Withdraw(
        accountNameOrId: String,
        wif: String,
        ethAddress: String,
        ethTokenId: String,
        value: String,
        feeAsset: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: TransactionResultCallback
    ) {
        try {
            val account = findAccount(accountNameOrId)
            checkOwnerAccount(wif, account)
            val privateKey = cryptoCoreComponent.decodeFromWif(wif)

            val processedAddress =
                    ethAddress.replace(EthAddressValidator.ADDRESS_PREFIX, "").toLowerCase()

            val operation =
                    WithdrawERC20Operation(
                            Account(account.getObjectId()),
                            processedAddress,
                            ERC20Token(ethTokenId),
                            value
                    )

            val transaction = configureTransaction(operation, privateKey, feeAsset)

            val broadcastTransaction = networkBroadcastApiService
                    .broadcastTransaction(transaction)

            broadcastCallback.onSuccess(true)
            resultCallback.processResult(broadcastTransaction)
        } catch (exception: Exception) {
            broadcastCallback.onError(exception as? LocalException ?: LocalException(exception))
            return
        }
    }

    override fun getERC20TokenByAddress(address: String, callback: Callback<ERC20Token>) {
        if (!EthAddressValidator.isAddressValid(address)) {
            callback.onError(LocalException("Invalid ERC20 token address"))
        } else {
            val processedAddress =
                address.replace(EthAddressValidator.ADDRESS_PREFIX, "")
            databaseApiService.getERC20Token(processedAddress).fold({ token ->
                val account = try {
                    findAccount(token.owner?.getObjectId())
                } catch (exception: LocalException) {
                    callback.onError(exception)
                    return@fold
                }
                token.owner = account
                callback.onSuccess(token)
            }, { error ->
                callback.onError(error)
            })
        }
    }

    override fun getERC20TokenByTokenId(tokenId: String, callback: Callback<ERC20Token>) {
        databaseApiService.getERC20Token(tokenId).fold({ token ->
            val account = try {
                findAccount(token.owner?.getObjectId())
            } catch (exception: LocalException) {
                callback.onError(exception)
                return@fold
            }
            token.owner = account
            callback.onSuccess(token)
        }, { error ->
            callback.onError(error)
        })
    }

    override fun checkERC20Token(contractId: String, callback: Callback<Boolean>) {
        databaseApiService.checkERC20Token(contractId, callback)
    }

    override fun getERC20AccountDeposits(
        accountNameOrId: String,
        callback: Callback<List<ERC20Deposit>>
    ) {
        try {
            val account = findAccount(accountNameOrId)
            databaseApiService.getERC20AccountDeposits(account.getObjectId(), callback)
        } catch (exception: Exception) {
            callback.onError(exception as? LocalException ?: LocalException(exception))
            return
        }
    }

    override fun getERC20AccountWithdrawals(
        accountNameOrId: String,
        callback: Callback<List<ERC20Withdrawal>>
    ) {
        try {
            val account = findAccount(accountNameOrId)
            databaseApiService.getERC20AccountWithdrawals(account.getObjectId(), callback)
        } catch (exception: Exception) {
            callback.onError(exception as? LocalException ?: LocalException(exception))
            return
        }
    }

    private fun findAccount(nameOrId: String?): Account {
        nameOrId ?: throw LocalException("Empty account id is not acceptable")
        val accountsMap =
            databaseApiService.getFullAccounts(listOf(nameOrId), false).dematerialize()
        return accountsMap[nameOrId]?.account
            ?: throw AccountNotFoundException("Unable to find required account $nameOrId")
    }
}