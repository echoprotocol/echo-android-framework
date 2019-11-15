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
    COMMITTEE_MEMBER_OBJECT(ObjectType.PROTOCOL_SPACE, 4),
    PROPOSAL_OBJECT(ObjectType.PROTOCOL_SPACE, 5),
    OPERATION_HISTORY_OBJECT(ObjectType.PROTOCOL_SPACE, 6),
    VESTING_BALANCE_OBJECT(ObjectType.PROTOCOL_SPACE, 7),
    BALANCE_OBJECT(ObjectType.PROTOCOL_SPACE, 8),
    FROZEN_BALANCE_OBJECT(ObjectType.PROTOCOL_SPACE, 9),
    COMMITTEE_FROZEN_BALANCE_OBJECT(ObjectType.PROTOCOL_SPACE, 10),
    CONTRACT_OBJECT(ObjectType.PROTOCOL_SPACE, 11),
    CONTRACT_RESULT_OBJECT(ObjectType.PROTOCOL_SPACE, 12),
    ETH_ADDRESS_OBJECT(ObjectType.PROTOCOL_SPACE, 13),
    DEPOSIT_ETH_OBJECT(ObjectType.PROTOCOL_SPACE, 14),
    WITHDRAW_ETH_OBJECT(ObjectType.PROTOCOL_SPACE, 15),
    ERC20_TOKEN_OBJECT(ObjectType.PROTOCOL_SPACE, 16),
    DEPOSIT_ERC20_TOKEN_OBJECT(ObjectType.PROTOCOL_SPACE, 17),
    WITHDRAW_ERC20_TOKEN_OBJECT(ObjectType.PROTOCOL_SPACE, 18),
    BTC_ADDRESS_OBJECT(ObjectType.PROTOCOL_SPACE, 19),
    BTC_INTERMEDIATE_DEPOSIT_OBJECT(ObjectType.PROTOCOL_SPACE, 20),
    BTC_DEPOSIT_OBJECT(ObjectType.PROTOCOL_SPACE, 21),
    BTC_WITHDRAW_OBJECT(ObjectType.PROTOCOL_SPACE, 22),
    BTC_AGGREGATING_OBJECT(ObjectType.PROTOCOL_SPACE, 23),
    OBJECT_TYPE_COUNT(ObjectType.PROTOCOL_SPACE, 24);

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
