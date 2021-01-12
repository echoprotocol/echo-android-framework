package org.echo.mobile.framework.facade.internal

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.exception.AccountNotFoundException
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.exception.NotFoundException
import org.echo.mobile.framework.facade.InformationFacade
import org.echo.mobile.framework.model.*
import org.echo.mobile.framework.model.operations.*
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

    override fun getBalance(nameOrId: String, asset: String, callback: Callback<AccountBalance>) =
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
    ): Result<LocalException, AccountBalance> = account?.let { notNullAccount ->
        val accountBalances = notNullAccount.accountBalances
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
                OperationType.CONTRACT_INTERNAL_CALL_OPERATION ->
                    processContractTransferOperation(
                            operation as ContractTransferOperation,
                            assetsRegistry
                    )
                OperationType.SIDECHAIN_ETH_CREATE_ADDRESS_OPERATION ->
                    processGenerateEthAddressOperation(
                            operation as GenerateEthereumAddressOperation,
                            accountsRegistry
                    )
                OperationType.SIDECHAIN_BTC_CREATE_ADDRESS_OPERATION ->
                    processGenerateBtcAddressOperation(
                            operation as GenerateBitcoinAddressOperation,
                            accountsRegistry
                    )
                OperationType.SIDECHAIN_ETH_WITHDRAW_OPERATION ->
                    processWithdrawEthOperation(
                            operation as WithdrawEthereumOperation,
                            accountsRegistry
                    )
                OperationType.SIDECHAIN_BTC_WITHDRAW_OPERATION ->
                    processWithdrawBtcOperation(
                            operation as WithdrawBitcoinOperation,
                            accountsRegistry
                    )
                OperationType.SIDECHAIN_ISSUE_OPERATION ->
                    processSidechainIssueOperation(
                            operation as SidechainIssueSocketOperation,
                            accountsRegistry,
                            assetsRegistry
                    )
                OperationType.SIDECHAIN_BURN_OPERATION ->
                    processSidechainBurnOperation(
                            operation as SidechainBurnSocketOperation,
                            accountsRegistry,
                            assetsRegistry
                    )
                OperationType.BALANCE_CLAIM_OPERATION ->
                    processBalanceClaimOperation(
                            operation as BalanceClaimOperation,
                            accountsRegistry,
                            assetsRegistry
                    )
                OperationType.BALANCE_FREEZE_OPERATION ->
                    processBalanceFreezeOperation(
                            operation as BalanceFreezeOperation,
                            accountsRegistry,
                            assetsRegistry
                    )
                OperationType.BALANCE_UNFREEZE_OPERATION ->
                    processBalanceUnfreezeOperation(
                            operation as BalanceUnfreezeOperation,
                            accountsRegistry,
                            assetsRegistry
                    )
                OperationType.REQUEST_BALANCE_UNFREEZE_OPERATION ->
                    processRequestBalanceUnfreezeOperation(
                            operation as RequestBalanceUnfreezeOperation,
                            accountsRegistry
                    )
                OperationType.SIDECHAIN_ERC20_REGISTER_TOKEN_OPERATION ->
                    processSidechainERC20RegisterTokenOperation(
                            operation as SidechainERC20RegisterTokenOperation,
                            accountsRegistry
                    )
                OperationType.SIDECHAIN_ERC20_ISSUE_OPERATION ->
                    processSidechainERC20IssueTokenOperation(
                            operation as SidechainERC20IssueSocketOperation,
                            accountsRegistry
                    )
                OperationType.SIDECHAIN_ERC20_BURN_OPERATION ->
                    processSidechainERC20BurnTokenOperation(
                            operation as SidechainERC20BurnSocketOperation,
                            accountsRegistry
                    )
                OperationType.SIDECHAIN_ERC20_DEPOSIT_TOKEN_OPERATION ->
                    processSidechainERC20DepositTokenOperation(
                            operation as SidechainERC20DepositSocketOperation,
                            accountsRegistry
                    )
                OperationType.SIDECHAIN_ERC20_WITHDRAW_TOKEN_OPERATION ->
                    processSidechainERC20WithdrawTokenOperation(
                            operation as WithdrawERC20Operation,
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

        fillAccounts(listOf(registrar), accountRegistry)

        accountRegistry[registrar]?.let { notNullAccount ->
            operation.registrar = notNullAccount
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

    private fun processContractTransferOperation(
            operation: ContractTransferOperation,
            assetsRegistry: MutableList<Asset>
    ) {
        val assetId = operation.value.asset.getObjectId()

        getAsset(assetId, assetsRegistry)?.let { notNullAsset ->
            operation.value.asset = notNullAsset
        }
    }

    private fun processGenerateEthAddressOperation(
            operation: GenerateEthereumAddressOperation,
            accountRegistry: MutableMap<String, Account>
    ) {
        val account = operation.account.getObjectId()

        fillAccounts(listOf(account), accountRegistry)

        accountRegistry[account]?.let { notNullAccount ->
            operation.account = notNullAccount
        }
    }

    private fun processGenerateBtcAddressOperation(
            operation: GenerateBitcoinAddressOperation,
            accountRegistry: MutableMap<String, Account>
    ) {
        val account = operation.account.getObjectId()

        fillAccounts(listOf(account), accountRegistry)

        accountRegistry[account]?.let { notNullAccount ->
            operation.account = notNullAccount
        }
    }

    private fun processWithdrawEthOperation(
            operation: WithdrawEthereumOperation,
            accountRegistry: MutableMap<String, Account>
    ) {
        val account = operation.account.getObjectId()

        fillAccounts(listOf(account), accountRegistry)

        accountRegistry[account]?.let { notNullAccount ->
            operation.account = notNullAccount
        }
    }

    private fun processWithdrawBtcOperation(
            operation: WithdrawBitcoinOperation,
            accountRegistry: MutableMap<String, Account>
    ) {
        val account = operation.account.getObjectId()

        fillAccounts(listOf(account), accountRegistry)

        accountRegistry[account]?.let { notNullAccount ->
            operation.account = notNullAccount
        }
    }

    private fun processSidechainIssueOperation(
            operation: SidechainIssueSocketOperation,
            accountRegistry: MutableMap<String, Account>,
            assetsRegistry: MutableList<Asset>
    ) {
        val account = operation.account.getObjectId()

        fillAccounts(listOf(account), accountRegistry)

        accountRegistry[account]?.let { notNullAccount ->
            operation.account = notNullAccount
        }

        val assetId = operation.value.asset.getObjectId()

        getAsset(assetId, assetsRegistry)?.let { notNullAsset ->
            operation.value.asset = notNullAsset
        }

        val withdrawId = operation.deposit.getObjectId()
        databaseApiService.getObjects(listOf(withdrawId), DepositMapper())
                .value { deposits ->
                    deposits.find { it.getObjectId() == withdrawId }?.let {
                        operation.deposit = it
                    }
                }
    }

    private fun processSidechainBurnOperation(
            operation: SidechainBurnSocketOperation,
            accountRegistry: MutableMap<String, Account>,
            assetsRegistry: MutableList<Asset>
    ) {
        val account = operation.account.getObjectId()

        fillAccounts(listOf(account), accountRegistry)

        accountRegistry[account]?.let { notNullAccount ->
            operation.account = notNullAccount
        }

        val assetId = operation.value.asset.getObjectId()

        getAsset(assetId, assetsRegistry)?.let { notNullAsset ->
            operation.value.asset = notNullAsset
        }

        val withdrawId = operation.withdraw.getObjectId()
        databaseApiService.getObjects(listOf(withdrawId), WithdrawMapper())
                .value { withdraws ->
                    withdraws.find { it.getObjectId() == withdrawId }?.let {
                        operation.withdraw = it
                    }
                }
    }

    private fun processBalanceClaimOperation(
            operation: BalanceClaimOperation,
            accountRegistry: MutableMap<String, Account>,
            assetsRegistry: MutableList<Asset>) {
        val accountId = operation.depositToAccount.getObjectId()

        fillAccounts(listOf(accountId), accountRegistry)

        accountRegistry[accountId]?.let { notNullAccount ->
            operation.depositToAccount = notNullAccount
        }

        val assetId = operation.totalClaimed.asset.getObjectId()

        getAsset(assetId, assetsRegistry)?.let { notNullAsset ->
            operation.totalClaimed.asset = notNullAsset
        }
    }

    private fun processBalanceFreezeOperation(
            operation: BalanceFreezeOperation,
            accountRegistry: MutableMap<String, Account>,
            assetsRegistry: MutableList<Asset>) {
        val accountId = operation.account.getObjectId()

        fillAccounts(listOf(accountId), accountRegistry)

        accountRegistry[accountId]?.let { notNullAccount ->
            operation.account = notNullAccount
        }

        val assetId = operation.amount.asset.getObjectId()

        getAsset(assetId, assetsRegistry)?.let { notNullAsset ->
            operation.amount.asset = notNullAsset
        }
    }

    private fun processBalanceUnfreezeOperation(
            operation: BalanceUnfreezeOperation,
            accountRegistry: MutableMap<String, Account>,
            assetsRegistry: MutableList<Asset>) {
        val accountId = operation.account.getObjectId()

        fillAccounts(listOf(accountId), accountRegistry)

        accountRegistry[accountId]?.let { notNullAccount ->
            operation.account = notNullAccount
        }

        val assetId = operation.amount.asset.getObjectId()

        getAsset(assetId, assetsRegistry)?.let { notNullAsset ->
            operation.amount.asset = notNullAsset
        }
    }

    private fun processRequestBalanceUnfreezeOperation(
            operation: RequestBalanceUnfreezeOperation,
            accountRegistry: MutableMap<String, Account>) {
        val accountId = operation.account.getObjectId()

        fillAccounts(listOf(accountId), accountRegistry)

        accountRegistry[accountId]?.let { notNullAccount ->
            operation.account = notNullAccount
        }
    }

    private fun processSidechainERC20RegisterTokenOperation(
            operation: SidechainERC20RegisterTokenOperation,
            accountRegistry: MutableMap<String, Account>
    ) {
        val account = operation.account.getObjectId()

        fillAccounts(listOf(account), accountRegistry)

        accountRegistry[account]?.let { notNullAccount ->
            operation.account = notNullAccount
        }
    }

    private fun processSidechainERC20IssueTokenOperation(
            operation: SidechainERC20IssueSocketOperation,
            accountRegistry: MutableMap<String, Account>
    ) {
        val account = operation.account.getObjectId()

        fillAccounts(listOf(account), accountRegistry)

        accountRegistry[account]?.let { notNullAccount ->
            operation.account = notNullAccount
        }

        val depositId = operation.erc20Deposit.getObjectId()
        databaseApiService.getObjects(listOf(depositId), Erc20DepositMapper())
                .value { deposits ->
                    deposits.find { it.getObjectId() == depositId }?.let {
                        operation.erc20Deposit = it
                    }
                }

        val tokenId = operation.erc20Token.getObjectId()
        databaseApiService.getObjects(listOf(tokenId), Erc20TokenMapper())
                .value { deposits ->
                    deposits.find { it.getObjectId() == tokenId }?.let {
                        operation.erc20Token = it
                    }
                }
    }

    private fun processSidechainERC20DepositTokenOperation(
            operation: SidechainERC20DepositSocketOperation,
            accountRegistry: MutableMap<String, Account>
    ) {
        val account = operation.account.getObjectId()
        val commetteeMember = operation.committeeMember.getObjectId()

        fillAccounts(listOf(account, commetteeMember), accountRegistry)

        accountRegistry[account]?.let { notNullAccount ->
            operation.account = notNullAccount
        }
        accountRegistry[commetteeMember]?.let { notNullAccount ->
            operation.committeeMember = notNullAccount
        }
    }

    private fun processSidechainERC20BurnTokenOperation(
            operation: SidechainERC20BurnSocketOperation,
            accountRegistry: MutableMap<String, Account>
    ) {
        val account = operation.account.getObjectId()

        fillAccounts(listOf(account), accountRegistry)

        accountRegistry[account]?.let { notNullAccount ->
            operation.account = notNullAccount
        }

        val depositId = operation.erc20Withdrawal.getObjectId()
        databaseApiService.getObjects(listOf(depositId), Erc20WithdrawalMapper())
                .value { deposits ->
                    deposits.find { it.getObjectId() == depositId }?.let {
                        operation.erc20Withdrawal = it
                    }
                }

        val tokenId = operation.erc20Token.getObjectId()
        databaseApiService.getObjects(listOf(tokenId), Erc20TokenMapper())
                .value { deposits ->
                    deposits.find { it.getObjectId() == tokenId }?.let {
                        operation.erc20Token = it
                    }
                }
    }

    private fun processSidechainERC20WithdrawTokenOperation(
            operation: WithdrawERC20Operation,
            accountRegistry: MutableMap<String, Account>
    ) {
        val account = operation.account.getObjectId()

        fillAccounts(listOf(account), accountRegistry)

        accountRegistry[account]?.let { notNullAccount ->
            operation.account = notNullAccount
        }

        val tokenId = operation.erc20Token.getObjectId()
        databaseApiService.getObjects(listOf(tokenId), Erc20TokenMapper())
                .value { deposits ->
                    deposits.find { it.getObjectId() == tokenId }?.let {
                        operation.erc20Token = it
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
