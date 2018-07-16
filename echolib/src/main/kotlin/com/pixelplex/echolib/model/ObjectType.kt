package com.pixelplex.echolib.model

/**
 * Enum type used to list all possible object types in Graphene blockchain
 *
 * <p>
 *      Every element contains information about it's type and space
 * </p>
 *
 * @author Dmitriy Bushuev
 */
enum class ObjectType(private val space: Int, private val type: Int) {

    BASE_OBJECT(ObjectType.PROTOCOL_SPACE, 1),
    ACCOUNT_OBJECT(ObjectType.PROTOCOL_SPACE, 2),
    ASSET_OBJECT(ObjectType.PROTOCOL_SPACE, 3),
    FORCE_SETTLEMENT_OBJECT(ObjectType.PROTOCOL_SPACE, 4),
    COMMITTEE_MEMBER_OBJECT(ObjectType.PROTOCOL_SPACE, 5),
    WITNESS_OBJECT(ObjectType.PROTOCOL_SPACE, 6),
    LIMIT_ORDER_OBJECT(ObjectType.PROTOCOL_SPACE, 7),
    CALL_ORDER_OBJECT(ObjectType.PROTOCOL_SPACE, 8),
    CUSTOM_OBJECT(ObjectType.PROTOCOL_SPACE, 9),
    PROPOSAL_OBJECT(ObjectType.PROTOCOL_SPACE, 10),
    OPERATION_HISTORY_OBJECT(ObjectType.PROTOCOL_SPACE, 11),
    WITHDRAW_PERMISSION_OBJECT(ObjectType.PROTOCOL_SPACE, 12),
    VESTING_BALANCE_OBJECT(ObjectType.PROTOCOL_SPACE, 13),
    WORKER_OBJECT(ObjectType.PROTOCOL_SPACE, 14),
    BALANCE_OBJECT(ObjectType.PROTOCOL_SPACE, 15),

    GLOBAL_PROPERTY_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 0),
    DYNAMIC_GLOBAL_PROPERTY_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 1),
    ASSET_DYNAMIC_DATA(ObjectType.IMPLEMENTATION_SPACE, 3),
    ASSET_BITASSET_DATA(ObjectType.IMPLEMENTATION_SPACE, 4),
    ACCOUNT_BALANCE_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 5),
    ACCOUNT_STATISTICS_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 6),
    TRANSACTION_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 7),
    BLOCK_SUMMARY_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 8),
    ACCOUNT_TRANSACTION_HISTORY_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 9),
    BLINDED_BALANCE_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 10),
    CHAIN_PROPERTY_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 11),
    WITNESS_SCHEDULE_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 12),
    BUDGET_RECORD_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 13),
    SPECIAL_AUTHORITY_OBJECT(ObjectType.IMPLEMENTATION_SPACE, 14);

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
