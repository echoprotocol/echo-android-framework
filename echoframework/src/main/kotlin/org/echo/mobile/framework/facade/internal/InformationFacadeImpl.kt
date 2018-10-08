package org.echo.mobile.framework.facade.internal

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.exception.NotFoundException
import org.echo.mobile.framework.facade.InformationFacade
import org.echo.mobile.framework.model.*
import org.echo.mobile.framework.model.operations.*
import org.echo.mobile.framework.processResult
import org.echo.mobile.framework.service.AccountHistoryApiService
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.support.*
import org.echo.mobile.framework.support.Result.Error
import org.echo.mobile.framework.support.Result.Value
import org.echo.mobile.framework.support.concurrent.future.FutureTask
import org.echo.mobile.framework.support.concurrent.future.completeCallback
import org.echo.mobile.framework.support.concurrent.future.wrapResult

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

    override fun getAccount(nameOrId: String, callback: Callback<FullAccount>) =
        findAccount(nameOrId,
            { fullAccount ->
                fullAccount?.let { notNullAccount ->
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
            accountBalances.firstOrNull { balance -> balance.asset?.getObjectId() == asset }?.let { balance ->
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
            .value { account -> accountId = account.account!!.getObjectId() }
            .error { error ->
                LOGGER.log("Unable to find account $nameOrId for history request", error)
                callback.onError(error)
                return
            }

        callback.processResult(accountHistoryApiService.getAccountHistory(
            accountId,
            transactionStartId,
            transactionStopId,
            limit
        ).map { history ->
            fillTransactionInformation(history)
        })
    }

    private fun getAccount(nameOrId: String): Result<LocalException, FullAccount> {
        val accountFuture = FutureTask<FullAccount>()

        getAccount(nameOrId, accountFuture.completeCallback())

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

                OperationType.ASSET_CREATE_OPERATION ->
                    processAssetCreateOperation(operation as CreateAssetOperation, accountsRegistry)

                OperationType.ASSET_ISSUE_OPERATION ->
                    processAssetIssueOperation(operation as IssueAssetOperation, accountsRegistry)

                OperationType.ACCOUNT_CREATE_OPERATION ->
                    processAccountCreateOperation(
                        operation as AccountCreateOperation,
                        accountsRegistry
                    )

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

    private fun processAssetCreateOperation(
        operation: CreateAssetOperation,
        accountRegistry: MutableMap<String, Account>
    ) {
        val accountId = operation.asset.issuer?.getObjectId() ?: return

        accountRegistry[accountId]?.let { account ->
            operation.asset.issuer = account
            return
        }

        databaseApiService.getFullAccounts(listOf(accountId), false)
            .value { accountsMap ->
                accountsMap[accountId]?.account?.let { notNullAccount ->
                    operation.asset.issuer = notNullAccount
                    accountRegistry[accountId] = notNullAccount
                }
            }
    }

    private fun processAssetIssueOperation(
        operation: IssueAssetOperation,
        accountRegistry: MutableMap<String, Account>
    ) {
        val fromAccountId = operation.issuer.getObjectId()
        val toAccountId = operation.issueToAccount.getObjectId()

        if (accountRegistry.containsKey(fromAccountId) && accountRegistry.containsKey(toAccountId)) {
            accountRegistry[fromAccountId]?.let { operation.issuer = it }
            accountRegistry[toAccountId]?.let { operation.issueToAccount = it }
            return
        }

        databaseApiService.getFullAccounts(listOf(fromAccountId, toAccountId), false)
            .value { accountsMap ->
                accountsMap[fromAccountId]?.account?.let { notNullFromAccount ->
                    operation.issuer = notNullFromAccount
                    accountRegistry[fromAccountId] = notNullFromAccount
                }
                accountsMap[toAccountId]?.account?.let { notNullToAccount ->
                    operation.issueToAccount = notNullToAccount
                    accountRegistry[toAccountId] = notNullToAccount
                }
            }
    }

    private fun processAccountCreateOperation(
        operation: AccountCreateOperation,
        accountRegistry: MutableMap<String, Account>
    ) {
        val registrar = operation.registrar.getObjectId()
        val referrer = operation.referrer.getObjectId()

        if (accountRegistry.containsKey(registrar) && accountRegistry.containsKey(referrer)) {
            accountRegistry[registrar]?.let { operation.registrar = it }
            accountRegistry[referrer]?.let { operation.referrer = it }
            return
        }

        databaseApiService.getFullAccounts(listOf(registrar, referrer), false)
            .value { accountsMap ->
                accountsMap[registrar]?.account?.let { notNullRegistrar ->
                    operation.registrar = notNullRegistrar
                    accountRegistry[registrar] = notNullRegistrar
                }
                accountsMap[referrer]?.account?.let { notNullReferrer ->
                    operation.referrer = notNullReferrer
                    accountRegistry[referrer] = notNullReferrer
                }
            }
    }

    companion object {
        private val LOGGER = LoggerCoreComponent.create(InformationFacadeImpl::class.java.name)
    }

}
