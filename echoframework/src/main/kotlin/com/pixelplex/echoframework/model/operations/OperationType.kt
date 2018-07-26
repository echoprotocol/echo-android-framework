package com.pixelplex.echoframework.model.operations

import com.pixelplex.echoframework.support.Converter

/**
 * Represents all blockchain operation types
 *
 * <a href="https://bitshares.org/doxygen/operations_8hpp_source.html">Source</a>
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
    ASSET_CLAIM_POOL_OPERATION,
    ASSET_UPDATE_ISSUER_OPERATION
}

/**
 * Maps operation id to required result class type
 */
class OperationTypeToClassConverter : Converter<Int, Class<*>?> {

    override fun convert(source: Int): Class<*>? = OPERATION_TYPE_REGISTRY[source]

    companion object {
        private val OPERATION_TYPE_REGISTRY = hashMapOf(
            OperationType.ACCOUNT_UPDATE_OPERATION.ordinal to AccountUpdateOperation::class.java,
            OperationType.TRANSFER_OPERATION.ordinal to TransferOperation::class.java
        )
    }

}
