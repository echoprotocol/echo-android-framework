package com.pixelplex.echoframework.facade.internal

import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.exception.NotFoundException
import com.pixelplex.echoframework.facade.InformationFacade
import com.pixelplex.echoframework.model.*
import com.pixelplex.echoframework.model.operations.AccountUpdateOperation
import com.pixelplex.echoframework.model.socketoperations.TransferOperation
import com.pixelplex.echoframework.service.AccountHistoryApiService
import com.pixelplex.echoframework.service.DatabaseApiService
import com.pixelplex.echoframework.support.*
import com.pixelplex.echoframework.support.Result.Error
import com.pixelplex.echoframework.support.Result.Value

/**
 * Implementation of [InformationFacade]
 *
 * <p>
 *     Delegates API call logic to [DatabaseApiService]
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class InformationFacadeImpl(
    private val databaseApiService: DatabaseApiService,
    private val accountHistoryApiService: AccountHistoryApiService
) :
    InformationFacade {

    override fun getAccount(nameOrId: String, callback: Callback<Account>) {
        val result = databaseApiService.getFullAccounts(listOf(nameOrId), false)

        result.fold({ accountMap ->
            val requiredAccount = accountMap[nameOrId]

            requiredAccount?.account?.let { account ->
                callback.onSuccess(account)
            } ?: callback.onError(NotFoundException("Account not found."))
        }, { error ->
            callback.onError(LocalException(error.message, error))
        })
    }

    override fun checkAccountReserved(nameOrId: String, callback: Callback<Boolean>) {
        val result = databaseApiService.getFullAccounts(listOf(nameOrId), false)

        result.fold({ accountMap ->
            val requiredAccount = accountMap[nameOrId]

            requiredAccount?.let {
                callback.onSuccess(true)
            } ?: callback.onSuccess(false)
        }, { error ->
            callback.onError(LocalException(error.message, error))
        })
    }

    override fun getBalance(nameOrId: String, asset: String, callback: Callback<Balance>) {
        val result = databaseApiService.getFullAccounts(listOf(nameOrId), false)

        result
            .map { accountsMap -> accountsMap[nameOrId] }
            .value { requiredAccount ->
                findBalance(requiredAccount, asset)
                    .value { balance ->
                        callback.onSuccess(balance)
                    }
                    .error { balanceError ->
                        callback.onError(balanceError)
                    }
            }
            .error { error ->
                callback.onError(LocalException(error.message, error))
            }
    }

    private fun findBalance(
        account: FullAccount?,
        asset: String
    ): Result<LocalException, Balance> =
        account?.let { notNullAccount ->
            val accountBalances = notNullAccount.balances
            if (accountBalances?.isEmpty() == false) {
                accountBalances.firstOrNull { balance -> balance.assetType == asset }
                    ?.let { balance ->
                        Value(balance)
                    }
                        ?: Error(
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
        accountHistoryApiService.getAccountHistory(
            nameOrId,
            transactionStartId,
            transactionStopId,
            limit
        ).map { history ->
            fillAccounts(history)
        }.value { fullAccountHistory ->
            callback.onSuccess(fullAccountHistory)
        }.error { error ->
            callback.onError(LocalException(error.message, error))
        }
    }

    private fun fillAccounts(history: HistoryResponse): HistoryResponse {
        val fullAccountTransactions = mutableListOf<HistoricalTransfer>()

        for (transaction in history.transactions) {
            val operation = transaction.operation

            if (operation == null) {
                fullAccountTransactions.add(transaction)
                continue
            }

            when (operation.type) {
                OperationType.ACCOUNT_UPDATE_OPERATION ->
                    processAccountUpdateOperation(operation as AccountUpdateOperation)

                OperationType.TRANSFER_OPERATION ->
                    processTransferOperation(operation as TransferOperation)

                else -> {
                }
            }

            fullAccountTransactions.add(transaction)
        }

        return HistoryResponse(fullAccountTransactions, history.hasMore)
    }

    private fun processAccountUpdateOperation(operation: AccountUpdateOperation) {
        val accountId = operation.account.getObjectId()

        databaseApiService.getFullAccounts(listOf(accountId), false)
            .map { accountsMap ->
                accountsMap[accountId]?.account
            }
            .map { account ->
                account?.let { notNullAccount ->
                    operation.account = notNullAccount
                }
            }
    }

    private fun processTransferOperation(operation: TransferOperation) {
        val fromAccountId = operation.from?.getObjectId() ?: ""
        val toAccountId = operation.to?.getObjectId() ?: ""

        databaseApiService.getFullAccounts(listOf(fromAccountId, toAccountId), false)
            .map { accountsMap ->
                val fromAccount = accountsMap[fromAccountId]?.account
                val toAccount = accountsMap[toAccountId]?.account
                Pair(fromAccount, toAccount)
            }
            .map { accounts ->
                accounts.first?.let { notNullFromAccount ->
                    operation.from = notNullFromAccount
                }
                accounts.second?.let { notNullToAccount ->
                    operation.to = notNullToAccount
                }
            }
    }

}
