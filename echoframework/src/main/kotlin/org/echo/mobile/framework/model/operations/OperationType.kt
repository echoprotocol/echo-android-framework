package org.echo.mobile.framework.model.operations

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
    LIMIT_ORDER_CREATE_OPERATION,
    LIMIT_ORDER_CANCEL_OPERATION,
    CALL_ORDER_UPDATE_OPERATION,
    FILL_ORDER_OPERATION,
    ACCOUNT_CREATE_OPERATION,
    ACCOUNT_UPDATE_OPERATION,
    ACCOUNT_WHITELIST_OPERATION,
    ACCOUNT_UPGRADE_OPERATION,
    ACCOUNT_TRANSFER_OPERATION,
    ASSET_CREATE_OPERATION,
    ASSET_UPDATE_OPERATION,
    ASSET_UPDATE_BITASSET_OPERATION,
    ASSET_UPDATE_FEED_PRODUCERS_OPERATION,
    ASSET_ISSUE_OPERATION,
    ASSET_RESERVE_OPERATION,
    ASSET_FUND_FEE_POOL_OPERATION,
    ASSET_SETTLE_OPERATION,
    ASSET_GLOBAL_SETTLE_OPERATION,
    ASSET_PUBLISH_FEED_OPERATION,
    WITNESS_CREATE_OPERATION,
    WITNESS_UPDATE_OPERATION,
    PROPOSAL_CREATE_OPERATION,
    PROPOSAL_UPDATE_OPERATION,
    PROPOSAL_DELETE_OPERATION,
    WITHDRAW_PERMISSION_CREATE_OPERATION,
    WITHDRAW_PERMISSION_UPDATE_OPERATION,
    WITHDRAW_PERMISSION_CLAIM_OPERATION,
    WITHDRAW_PERMISSION_DELETE_OPERATION,
    COMMITTEE_MEMBER_CREATE_OPERATION,
    COMMITTEE_MEMBER_UPDATE_OPERATION,
    COMMITTEE_MEMBER_UPDATE_GLOBAL_PARAMETERS_OPERATION,
    VESTING_BALANCE_CREATE_OPERATION,
    VESTING_BALANCE_WITHDRAW_OPERATION,
    WORKER_CREATE_OPERATION,
    CUSTOM_OPERATION,
    ASSERT_OPERATION,
    BALANCE_CLAIM_OPERATION,
    OVERRIDE_TRANSFER_OPERATION,
    TRANSFER_TO_BLIND_OPERATION,
    BLIND_TRANSFER_OPERATION,
    TRANSFER_FROM_BLIND_OPERATION,
    ASSET_SETTLE_CANCEL_OPERATION,  // VIRTUAL
    ASSET_CLAIM_FEES_OPERATION,
    FBA_DISTRIBUTE_OPERATION,       //VIRTUAL
    BID_COLLATERAL_OPERATION,
    EXECUTE_BID_OPERATION,          //VIRTUAL
    CONTRACT_CREATE_OPERATION,
    CONTRACT_CALL_OPERATION,
    CONTRACT_TRANSFER_OPERATION,     //VIRTUAL
    CHANGE_SIDECHAIN_CONFIG_OPERATION
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
            OperationType.CONTRACT_TRANSFER_OPERATION.ordinal to ContractTransferOperation::class.java
        )
    }

}
