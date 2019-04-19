package org.echo.mobile.framework.model

/**
 * Enum type used to list all possible object types in Graphene blockchain
 *
 * Every element contains information about it's type and space
 *
 * @author Dmitriy Bushuev
 */
enum class ObjectType(private val space: Int, private val type: Int) {

    NULL_OBJECT(ObjectType.PROTOCOL_SPACE, 0),
    BASE_OBJECT(ObjectType.PROTOCOL_SPACE, 1),
    ACCOUNT_OBJECT(ObjectType.PROTOCOL_SPACE, 2),
    ASSET_OBJECT(ObjectType.PROTOCOL_SPACE, 3),
    FORCE_SETTLEMENT_OBJECT(ObjectType.PROTOCOL_SPACE, 4),
    COMMITTEE_MEMBER_OBJECT(ObjectType.PROTOCOL_SPACE, 5),
    LIMIT_ORDER_OBJECT(ObjectType.PROTOCOL_SPACE, 6),
    CALL_ORDER_OBJECT(ObjectType.PROTOCOL_SPACE, 7),
    CUSTOM_OBJECT(ObjectType.PROTOCOL_SPACE, 8),
    PROPOSAL_OBJECT(ObjectType.PROTOCOL_SPACE, 9),
    OPERATION_HISTORY_OBJECT(ObjectType.PROTOCOL_SPACE, 10),
    WITHDRAW_PERMISSION_OBJECT(ObjectType.PROTOCOL_SPACE, 11),
    VESTING_BALANCE_OBJECT(ObjectType.PROTOCOL_SPACE, 12),
    BALANCE_OBJECT(ObjectType.PROTOCOL_SPACE, 13),
    CONTRACT_OBJECT(ObjectType.PROTOCOL_SPACE, 14),
    CONTRACT_RESULT_OBJECT(ObjectType.PROTOCOL_SPACE, 15),
    BLOCK_RESULT_OBJECT(ObjectType.PROTOCOL_SPACE, 16),
    SIDECHAIN_RESULT_OBJECT(ObjectType.PROTOCOL_SPACE, 17),
    OBJECT_TYPE_COUNT(ObjectType.PROTOCOL_SPACE, 18),


    GLOBAL_PROPERTY_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 0),
    DYNAMIC_GLOBAL_PROPERTY_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 1),
    RESERVED_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 2),
    ASSET_DYNAMIC_DATA(ObjectType.IMPLEMENTATION_SPACE, 3),
    ASSET_BITASSET_DATA(ObjectType.IMPLEMENTATION_SPACE, 4),
    ACCOUNT_BALANCE_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 5),
    ACCOUNT_STATISTICS_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 6),
    TRANSACTION_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 7),
    BLOCK_SUMMARY_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 8),
    ACCOUNT_TRANSACTION_HISTORY_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 9),
    BLINDED_BALANCE_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 10),
    CHAIN_PROPERTY_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 11),
    BUDGET_RECORD_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 12),
    SPECIAL_AUTHORITY_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 13),
    BUYBACK_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 14),
    COLLATERAL_BID_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 15),
    CONTRACT_BALANCE_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 16),
    CONTRACT_HISTORY_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 17),
    CONTRACT_STATISTICS_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 18);

    /**
     * This method is used to return the generic object type in the form space.type.0.
     *
     * Not to be confused with [GrapheneObject.getObjectId], which will return
     * the full object id in the form space.type.id.
     *
     * @return: The generic object type
     */
    val genericObjectId: String
        get() = "$space.$type.0"

    companion object {

        const val PROTOCOL_SPACE = 1
        const val IMPLEMENTATION_SPACE = 2

        /**
         * Finds specific object type by [space] and [type]
         *
         * @return Required object type if exists, otherwise null
         */
        @JvmStatic
        fun get(space: Int, type: Int) =
            ObjectType.values().firstOrNull { objectType ->
                objectType.space == space && objectType.type == type
            }

    }

}
