package org.echo.mobile.framework.model.operations

import org.echo.mobile.framework.model.GenerateBitcoinAddressOperation
import org.echo.mobile.framework.support.Converter

/**
 * Represents all blockchain operation types
 *
 * <a href="https://bitshares.org/doxygen/operations_8hpp_source.html">Source</a>
 *
 * Virtual operations is using only for parsing history results
 *
 * @author Daria Pechkovskaya
 */
enum class OperationType {
    TRANSFER_OPERATION,
    TRANSFER_TO_ADDRESS_OPERATION,
    OVERRIDE_TRANSFER_OPERATION,
    ACCOUNT_CREATE_OPERATION,
    ACCOUNT_UPDATE_OPERATION,
    ACCOUNT_WHITELIST_OPERATION,
    ACCOUNT_ADDRESS_CREATE_OPERATION,
    ASSET_CREATE_OPERATION,
    ASSET_UPDATE_OPERATION,
    ASSET_UPDATE_BITASSET_OPERATION,
    ASSET_UPDATE_FEED_PRODUCERS_OPERATION,
    ASSET_ISSUE_OPERATION,
    ASSET_RESERVE_OPERATION,
    ASSET_FUND_FEE_POOL_OPERATION,
    ASSET_PUBLISH_FEED_OPERATION,
    ASSET_CLAIM_FEES_OPERATION,
    PROPOSAL_CREATE_OPERATION,
    PROPOSAL_UPDATE_OPERATION,
    PROPOSAL_DELETE_OPERATION,
    COMMITTEE_MEMBER_CREATE_OPERATION,
    COMMITTEE_MEMBER_UPDATE_OPERATION,
    COMMITTEE_MEMBER_UPDATE_GLOBAL_PARAMETERS_OPERATION,
    COMMITTEE_MEMBER_ACTIVATE_OPERATION,
    COMMITTEE_MEMBER_DEACTIVATE_OPERATION,
    COMMITTEE_FROZEN_BALANCE_DEPOSIT_OPERATION,
    COMMITTEE_FROZEN_BALANCE_WITHDRAW_OPERATION,
    VESTING_BALANCE_CREATE_OPERATION,
    VESTING_BALANCE_WITHDRAW_OPERATION,
    BALANCE_CLAIM_OPERATION,
    BALANCE_FREEZE_OPERATION,
    BALANCE_UNFREEZE_OPERATION,
    CONTRACT_CREATE_OPERATION,
    CONTRACT_CALL_OPERATION,
    CONTRACT_INTERNAL_CREATE_OPERATION, // VIRTUAL
    CONTRACT_INTERNAL_CALL_OPERATION, // VIRTUAL
    CONTRACT_SELFDESTRUCT_OPERATION, // VIRTUAL
    CONTRACT_UPDATE_OPERATION,
    CONTRACT_FUND_POOL_OPERATION,
    CONTRACT_WHITELIST_OPERATION,
    SIDECHAIN_ETH_CREATE_ADDRESS_OPERATION,
    SIDECHAIN_ETH_APPROVE_ADDRESS_OPERATION,
    SIDECHAIN_ETH_DEPOSIT_OPERATION,
    SIDECHAIN_ETH_WITHDRAW_OPERATION,
    SIDECHAIN_ETH_APPROVE_WITHDRAW_OPERATION,
    SIDECHAIN_ISSUE_OPERATION,          // VIRTUAL
    SIDECHAIN_BURN_OPERATION,          // VIRTUAL
    SIDECHAIN_ERC20_REGISTER_TOKEN_OPERATION,
    SIDECHAIN_ERC20_DEPOSIT_TOKEN_OPERATION,
    SIDECHAIN_ERC20_WITHDRAW_TOKEN_OPERATION,
    SIDECHAIN_ERC20_APPROVE_TOKEN_WITHDRAW_OPERATION,
    SIDECHAIN_ERC20_ISSUE_OPERATION, // VIRTUAL
    SIDECHAIN_ERC20_BURN_OPERATION, // VIRTUAL
    SIDECHAIN_BTC_CREATE_ADDRESS_OPERATION,
    SIDECHAIN_BTC_CREATE_INTERMEDIATE_DEPOSIT_OPERATION,
    SIDECHAIN_BTC_INTERMEDIATE_DEPOSIT_OPERATION,
    SIDECHAINE_BTC_DEPOSIT_OPERATION,
    SIDECHAIN_BTC_WITHDRAW_OPERATION,
    SIDECHAIN_BTC_APPROVE_WITHDRAW_OPERATION,
    SIDECHAIN_BTC_AGGREGATE_OPERATION,
    BLOCK_REWARD_OPERATION
}

/**
 * Maps operation id to required result class type
 */
class OperationTypeToClassConverter : Converter<Int, Class<*>?> {

    override fun convert(source: Int): Class<*>? = operationTypeRegistry[source]

    companion object {
        private val operationTypeRegistry = hashMapOf(
            OperationType.ACCOUNT_UPDATE_OPERATION.ordinal to AccountUpdateOperation::class.java,
            OperationType.TRANSFER_OPERATION.ordinal to TransferOperation::class.java,
            OperationType.ASSET_CREATE_OPERATION.ordinal to CreateAssetOperation::class.java,
            OperationType.ASSET_ISSUE_OPERATION.ordinal to IssueAssetOperation::class.java,
            OperationType.TRANSFER_OPERATION.ordinal to TransferOperation::class.java,
            OperationType.ACCOUNT_CREATE_OPERATION.ordinal to AccountCreateOperation::class.java,
            OperationType.CONTRACT_CREATE_OPERATION.ordinal to ContractCreateOperation::class.java,
            OperationType.CONTRACT_CALL_OPERATION.ordinal to ContractCallOperation::class.java,
            OperationType.CONTRACT_INTERNAL_CALL_OPERATION.ordinal to ContractTransferOperation::class.java,
            OperationType.SIDECHAIN_ETH_CREATE_ADDRESS_OPERATION.ordinal to GenerateEthereumAddressOperation::class.java,
            OperationType.SIDECHAIN_ETH_WITHDRAW_OPERATION.ordinal to WithdrawEthereumOperation::class.java,
            OperationType.SIDECHAIN_ISSUE_OPERATION.ordinal to SidechainIssueSocketOperation::class.java,
            OperationType.SIDECHAIN_BURN_OPERATION.ordinal to SidechainBurnSocketOperation::class.java,
            OperationType.SIDECHAIN_BTC_CREATE_ADDRESS_OPERATION.ordinal to GenerateBitcoinAddressOperation::class.java,
            OperationType.BLOCK_REWARD_OPERATION.ordinal to BlockRewardOperation::class.java,
            OperationType.SIDECHAIN_BTC_WITHDRAW_OPERATION.ordinal to WithdrawBitcoinOperation::class.java,
            OperationType.BLOCK_REWARD_OPERATION.ordinal to BlockRewardOperation::class.java,
            OperationType.SIDECHAIN_ERC20_REGISTER_TOKEN_OPERATION.ordinal to SidechainERC20RegisterTokenOperation::class.java,
            OperationType.SIDECHAIN_ERC20_WITHDRAW_TOKEN_OPERATION.ordinal to WithdrawERC20Operation::class.java,
            OperationType.SIDECHAIN_ERC20_ISSUE_OPERATION.ordinal to SidechainERC20IssueSocketOperation::class.java,
            OperationType.SIDECHAIN_ERC20_BURN_OPERATION.ordinal to SidechainERC20BurnSocketOperation::class.java,
            OperationType.SIDECHAIN_ERC20_DEPOSIT_TOKEN_OPERATION.ordinal to SidechainERC20DepositSocketOperation::class.java
        )
    }

}
