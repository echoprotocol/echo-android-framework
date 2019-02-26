package org.echo.mobile.framework.facade.internal

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.exception.AccountNotFoundException
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.exception.NotFoundException
import org.echo.mobile.framework.facade.InformationFacade
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.Balance
import org.echo.mobile.framework.model.BaseOperation
import org.echo.mobile.framework.model.Block
import org.echo.mobile.framework.model.FullAccount
import org.echo.mobile.framework.model.GlobalProperties
import org.echo.mobile.framework.model.HistoricalTransfer
import org.echo.mobile.framework.model.HistoryResponse
import org.echo.mobile.framework.model.HistoryResult
import org.echo.mobile.framework.model.operations.AccountCreateOperation
import org.echo.mobile.framework.model.operations.AccountUpdateOperation
import org.echo.mobile.framework.model.operations.ContractCallOperation
import org.echo.mobile.framework.model.operations.ContractCreateOperation
import org.echo.mobile.framework.model.operations.ContractOperation
import org.echo.mobile.framework.model.operations.CreateAssetOperation
import org.echo.mobile.framework.model.operations.IssueAssetOperation
import org.echo.mobile.framework.model.operations.OperationType
import org.echo.mobile.framework.model.operations.TransferOperation
import org.echo.mobile.framework.processResult
import org.echo.mobile.framework.service.AccountHistoryApiService
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.support.Result
import org.echo.mobile.framework.support.Result.Error
import org.echo.mobile.framework.support.Result.Value
import org.echo.mobile.framework.support.concurrent.future.FutureTask
import org.echo.mobile.framework.support.concurrent.future.completeCallback
import org.echo.mobile.framework.support.concurrent.future.wrapResult
import org.echo.mobile.framework.support.error
import org.echo.mobile.framework.support.map
import org.echo.mobile.framework.support.parse
import org.echo.mobile.framework.support.value

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
                } ?: callback.onError(AccountNotFoundException("Account not found."))
            },
            { error ->
                callback.onError(LocalException(error.message, error))
            })

    override fun getAccountsByWif(wif: String, callback: Callback<List<FullAccount>>) {
        databaseApiService.getAccountsByWif(listOf(wif))
            .value { accountsMap -> callback.onSuccess(accountsMap[wif]?.toList() ?: listOf()) }
            .error { error -> callback.onError(error) }
    }

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

    override fun getGlobalProperties(callback: Callback<GlobalProperties>) =
        databaseApiService.getGlobalProperties(callback)

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
    } ?: Error(AccountNotFoundException("Account not found."))

    override fun getAccountHistory(
        nameOrId: String,
        transactionStartId: String,
        transactionStopId: String,
        limit: Int,
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
        val assetsRegistry = mutableListOf<Asset>()

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

            fillFeeAssets(operation, assetsRegistry)

            when (operation.type) {
                OperationType.ACCOUNT_UPDATE_OPERATION ->
                    processAccountUpdateOperation(
                        operation as AccountUpdateOperation,
                        accountsRegistry
                    )

                OperationType.TRANSFER_OPERATION ->
                    processTransferOperation(
                        operation as TransferOperation,
                        accountsRegistry,
                        assetsRegistry
                    )

                OperationType.ASSET_CREATE_OPERATION ->
                    processAssetCreateOperation(
                        operation as CreateAssetOperation,
                        accountsRegistry,
                        assetsRegistry
                    )

                OperationType.ASSET_ISSUE_OPERATION ->
                    processAssetIssueOperation(
                        operation as IssueAssetOperation,
                        accountsRegistry,
                        assetsRegistry
                    )

                OperationType.ACCOUNT_CREATE_OPERATION ->
                    processAccountCreateOperation(
                        operation as AccountCreateOperation,
                        accountsRegistry
                    )

                OperationType.CONTRACT_CREATE_OPERATION ->
                    processContractOperation(
                        operation as ContractCreateOperation,
                        transaction.result,
                        accountsRegistry,
                        assetsRegistry
                    )

                OperationType.CONTRACT_CALL_OPERATION ->
                    processContractOperation(
                        operation as ContractCallOperation,
                        transaction.result,
                        accountsRegistry,
                        assetsRegistry
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

        fillAccounts(listOf(accountId), accountRegistry)

        accountRegistry[accountId]?.let { notNullAccount ->
            operation.account = notNullAccount
        }
    }

    private fun processTransferOperation(
        operation: TransferOperation,
        accountRegistry: MutableMap<String, Account>,
        assetsRegistry: MutableList<Asset>
    ) {
        val transferAssetId = operation.transferAmount.asset.getObjectId()

        getAsset(transferAssetId, assetsRegistry)?.let { notNullAsset ->
            operation.transferAmount.asset = notNullAsset
        }

        val fromAccountId = operation.from?.getObjectId() ?: ""
        val toAccountId = operation.to?.getObjectId() ?: ""

        fillAccounts(listOf(fromAccountId, toAccountId), accountRegistry)

        accountRegistry[fromAccountId]?.let { notNullAccount ->
            operation.from = notNullAccount
        }
        accountRegistry[toAccountId]?.let { notNullAccount ->
            operation.to = notNullAccount
        }
    }

    private fun processAssetCreateOperation(
        operation: CreateAssetOperation,
        accountRegistry: MutableMap<String, Account>,
        assetsRegistry: MutableList<Asset>
    ) {
        val createdAssetSymbol = operation.asset.symbol

        if (createdAssetSymbol != null) {
            getAssetBySymbol(createdAssetSymbol, assetsRegistry)?.let { notNullAsset ->
                operation.asset = notNullAsset
            }
        }

        val accountId = operation.asset.issuer?.getObjectId() ?: return

        fillAccounts(listOf(accountId), accountRegistry)

        accountRegistry[accountId]?.let { notNullAccount ->
            operation.asset.issuer = notNullAccount
        }
    }

    private fun processAssetIssueOperation(
        operation: IssueAssetOperation,
        accountRegistry: MutableMap<String, Account>,
        assetsRegistry: MutableList<Asset>
    ) {
        val issueAssetId = operation.assetToIssue.asset.getObjectId()

        getAsset(issueAssetId, assetsRegistry)?.let { notNullAsset ->
            operation.assetToIssue.asset = notNullAsset
        }

        val fromAccountId = operation.issuer.getObjectId()
        val toAccountId = operation.issueToAccount.getObjectId()

        fillAccounts(listOf(fromAccountId, toAccountId), accountRegistry)

        accountRegistry[fromAccountId]?.let { notNullAccount ->
            operation.issuer = notNullAccount
        }
        accountRegistry[toAccountId]?.let { notNullAccount ->
            operation.issueToAccount = notNullAccount
        }
    }

    private fun processAccountCreateOperation(
        operation: AccountCreateOperation,
        accountRegistry: MutableMap<String, Account>
    ) {
        val registrar = operation.registrar.getObjectId()
        val referrer = operation.referrer.getObjectId()

        fillAccounts(listOf(registrar, referrer), accountRegistry)

        accountRegistry[registrar]?.let { notNullAccount ->
            operation.registrar = notNullAccount
        }
        accountRegistry[referrer]?.let { notNullAccount ->
            operation.referrer = notNullAccount
        }
    }

    private fun processContractOperation(
        operation: ContractOperation,
        historyResult: HistoryResult?,
        accountRegistry: MutableMap<String, Account>,
        assetsRegistry: MutableList<Asset>
    ) {
        val assetId = operation.value.asset.getObjectId()

        getAsset(assetId, assetsRegistry)?.let { notNullAsset ->
            operation.value.asset = notNullAsset
        }

        val registrar = operation.registrar.getObjectId()

        fillAccounts(listOf(registrar), accountRegistry)

        accountRegistry[registrar]?.let { notNullAccount ->
            operation.registrar = notNullAccount
        }

        historyResult?.objectId?.let { resultId ->
            databaseApiService.getContractResult(resultId)
                .value { contractResult ->
                    operation.contractResult = contractResult
                }
        }
    }

    private fun fillFeeAssets(operation: BaseOperation, assetsRegistry: MutableList<Asset>) {
        val assetId = operation.fee.asset.getObjectId()

        getAsset(assetId, assetsRegistry)?.let { notNullAsset ->
            operation.fee.asset = notNullAsset
        }
    }

    private fun getAsset(assetId: String, assetsRegistry: MutableList<Asset>): Asset? {
        val cachedAsset = assetsRegistry.find { it.getObjectId() == assetId }

        cachedAsset?.let { notNullCachedAsset ->
            return notNullCachedAsset
        }

        databaseApiService.getAssets(listOf(assetId))
            .value { assets ->
                assets.find { it.getObjectId() == assetId }?.let { notNullAsset ->
                    assetsRegistry.add(notNullAsset)
                    return notNullAsset
                }
            }
        return null
    }

    private fun getAssetBySymbol(symbol: String, assetsRegistry: MutableList<Asset>): Asset? {
        val cachedAsset = assetsRegistry.find { it.symbol == symbol }

        cachedAsset?.let { notNullCachedAsset ->
            return notNullCachedAsset
        }

        databaseApiService.lookupAssetsSymbols(listOf(symbol))
            .value { assets ->
                assets.find { it.symbol == symbol }?.let { notNullAsset ->
                    assetsRegistry.add(notNullAsset)
                    return notNullAsset
                }
            }
        return null
    }

    private fun fillAccounts(ids: List<String>, accountsRegistry: MutableMap<String, Account>) {
        if (accountsRegistry.keys.containsAll(ids)) {
            return
        }

        databaseApiService.getFullAccounts(ids, false)
            .value { accountsMap ->
                accountsMap.forEach { (key, value) ->
                    value.account?.let { account ->
                        accountsRegistry[key] = account
                    }
                }
            }
    }

    companion object {
        private val LOGGER = LoggerCoreComponent.create(InformationFacadeImpl::class.java.name)
    }

}
