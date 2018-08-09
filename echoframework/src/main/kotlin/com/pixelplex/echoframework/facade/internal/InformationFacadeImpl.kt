package com.pixelplex.echoframework.facade.internal

import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.core.logger.internal.LoggerCoreComponent
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.exception.NotFoundException
import com.pixelplex.echoframework.facade.InformationFacade
import com.pixelplex.echoframework.model.*
import com.pixelplex.echoframework.model.operations.AccountUpdateOperation
import com.pixelplex.echoframework.model.operations.OperationType
import com.pixelplex.echoframework.model.operations.TransferOperation
import com.pixelplex.echoframework.service.AccountHistoryApiService
import com.pixelplex.echoframework.service.DatabaseApiService
import com.pixelplex.echoframework.support.*
import com.pixelplex.echoframework.support.Result.Error
import com.pixelplex.echoframework.support.Result.Value
import com.pixelplex.echoframework.support.concurrent.future.FutureTask
import com.pixelplex.echoframework.support.concurrent.future.wrapResult

/**
 * Implementation of [InformationFacade]
 *
 * Delegates API call logic to [DatabaseApiService]
 *
 * @author Dmitriy Bushuev
 */
class InformationFacadeImpl(
    private val databaseApiService: DatabaseApiService,
    private val accountHistoryApiService: AccountHistoryApiService
) : InformationFacade {

    override fun getAccount(nameOrId: String, callback: Callback<Account>) =
        findAccount(nameOrId,
            { fullAccount ->
                fullAccount?.account?.let { notNullAccount ->
                    callback.onSuccess(notNullAccount)
                } ?: callback.onError(NotFoundException("Account not found."))
            },
            { error ->
                callback.onError(LocalException(error.message, error))
            })

    override fun checkAccountReserved(nameOrId: String, callback: Callback<Boolean>) =
        findAccount(nameOrId,
            { fullAccount ->
                fullAccount?.let {
                    callback.onSuccess(true)
                } ?: callback.onSuccess(false)
            },
            { error ->
                callback.onError(LocalException(error.message, error))
            })

    override fun getBalance(nameOrId: String, asset: String, callback: Callback<Balance>) =
        findAccount(nameOrId,
            { fullAccount ->
                findBalance(fullAccount, asset)
                    .value { balance ->
                        callback.onSuccess(balance)
                    }
                    .error { balanceError ->
                        LOGGER.log(
                            "Unable to find account balances for required asset = $asset",
                            balanceError
                        )
                        callback.onError(balanceError)
                    }
            },
            { error ->
                callback.onError(LocalException(error.message, error))
            })

    private fun findAccount(
        nameOrId: String,
        success: (FullAccount?) -> Unit,
        failure: (Exception) -> Unit
    ) {
        databaseApiService.getFullAccounts(listOf(nameOrId), false)
            .map { accountMap -> accountMap[nameOrId] }
            .value { account -> success(account) }
            .error { error -> failure(error) }
    }

    private fun findBalance(
        account: FullAccount?,
        asset: String
    ): Result<LocalException, Balance> = account?.let { notNullAccount ->
        val accountBalances = notNullAccount.balances
        if (accountBalances?.isEmpty() == false) {
            accountBalances.firstOrNull { balance -> balance.assetType == asset }?.let { balance ->
                Value(balance)
            } ?: Error(
                NotFoundException("Account balance with asset type = $asset is not found")
            )
        } else {
            Error(LocalException("Account balances are empty."))
        }
    } ?: Error(NotFoundException("Account not found."))

    override fun getAccountHistory(
        nameOrId: String,
        transactionStartId: String,
        transactionStopId: String,
        limit: Int,
        asset: String,
        callback: Callback<HistoryResponse>
    ) {
        var accountId: String = nameOrId

        getAccount(nameOrId)
            .value { account -> accountId = account.getObjectId() }
            .error { error ->
                LOGGER.log("Unable to find account $nameOrId for history request", error)
                callback.onError(error)
                return
            }

        accountHistoryApiService.getAccountHistory(
            accountId,
            transactionStartId,
            transactionStopId,
            limit
        ).map { history ->
            fillTransactionInformation(history)
        }.value { fullAccountHistory ->
            callback.onSuccess(fullAccountHistory)
        }.error { error ->
            callback.onError(LocalException(error.message, error))
        }
    }

    private fun getAccount(nameOrId: String): Result<LocalException, Account> {
        val accountFuture = FutureTask<Account>()

        getAccount(nameOrId, object : Callback<Account> {

            override fun onSuccess(result: Account) {
                accountFuture.setComplete(result)
            }

            override fun onError(error: LocalException) {
                accountFuture.setComplete(error)
            }

        })

        return accountFuture.wrapResult()
    }

    private fun fillTransactionInformation(history: HistoryResponse): HistoryResponse {
        val fullAccountTransactions = mutableListOf<HistoricalTransfer>()

        // Need to save all requested additional information to avoid unnecessary calls
        val blocks = mutableMapOf<Long, Block>()
        val accountsRegistry = mutableMapOf<String, Account>()

        for (transaction in history.transactions) {
            if (!blocks.containsKey(transaction.blockNum)) {
                databaseApiService.getBlock(transaction.blockNum.toString())
                    .value { block ->
                        blocks[transaction.blockNum] = block
                    }
            }

            transaction.timestamp = blocks[transaction.blockNum]?.timestamp?.parse(default = null)

            val operation = transaction.operation

            if (operation == null) {
                fullAccountTransactions.add(transaction)
                continue
            }

           when (operation.type) {
                OperationType.ACCOUNT_UPDATE_OPERATION ->
                    processAccountUpdateOperation(
                        operation as AccountUpdateOperation,
                        accountsRegistry
                    )

                OperationType.TRANSFER_OPERATION ->
                    processTransferOperation(operation as TransferOperation, accountsRegistry)

                else -> {
                }
            }

            fullAccountTransactions.add(transaction)
        }

        return HistoryResponse(fullAccountTransactions)
    }

    private fun processAccountUpdateOperation(
        operation: AccountUpdateOperation,
        accountRegistry: MutableMap<String, Account>
    ) {
        val accountId = operation.account.getObjectId()

        accountRegistry[accountId]?.let { account ->
            operation.account = account
            return
        }

        databaseApiService.getFullAccounts(listOf(accountId), false)
            .value { accountsMap ->
                accountsMap[accountId]?.account?.let { notNullAccount ->
                    operation.account = notNullAccount
                    accountRegistry[accountId] = notNullAccount
                }
            }
    }

    private fun processTransferOperation(
        operation: TransferOperation,
        accountRegistry: MutableMap<String, Account>
    ) {
        val fromAccountId = operation.from?.getObjectId() ?: ""
        val toAccountId = operation.to?.getObjectId() ?: ""

        if (accountRegistry.containsKey(fromAccountId) && accountRegistry.containsKey(toAccountId)) {
            operation.from = accountRegistry[fromAccountId]
            operation.to = accountRegistry[toAccountId]
            return
        }

        databaseApiService.getFullAccounts(listOf(fromAccountId, toAccountId), false)
            .value { accountsMap ->
                accountsMap[fromAccountId]?.account?.let { notNullFromAccount ->
                    operation.from = notNullFromAccount
                    accountRegistry[fromAccountId] = notNullFromAccount
                }
                accountsMap[toAccountId]?.account?.let { notNullToAccount ->
                    operation.to = notNullToAccount
                    accountRegistry[toAccountId] = notNullToAccount
                }
            }
    }

    companion object {
        private val LOGGER = LoggerCoreComponent.create(InformationFacadeImpl::class.java.name)
    }

}
