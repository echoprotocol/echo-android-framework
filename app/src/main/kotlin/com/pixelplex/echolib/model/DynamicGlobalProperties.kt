package com.pixelplex.echolib.model

import java.io.Serializable
import java.util.*

/**
 * Represents account model in Graphene blockchain
 * (@see https://bitshares.org/doxygen/classgraphene_1_1chain_1_1dynamic__global__property__object.html)
 *
 * @author Daria Pechkovskaya
 */
class DynamicGlobalProperties(
    id: String,
    val headBlockNumber: Long = 0,
    val headBlockId: String,
    val time: Date,
    val currentWitness: String,
    val nextMaintenanceTime: Date,
    val lastBudgetTime: String,
    val witnessBudget: Long = 0,
    val accountsRegisteredThisInterval: Long = 0,
    val recentlyMissedCount: Long = 0,
    val currentAslot: Long = 0,
    val recentSlotsFilled: String,
    val dynamicFlags: Int = 0,
    val lastIrreversibleBlockNum: Long = 0

) : GrapheneObject(id), Serializable {

    companion object {
        const val KEY_HEAD_BLOCK_NUMBER = "head_block_number"
        const val KEY_HEAD_BLOCK_ID = "head_block_id"
        const val KEY_TIME = "time"
        const val KEY_CURRENT_WITNESS = "current_witness"
        const val KEY_NEXT_MAINTENANCE_TIME = "next_maintenance_time"
        const val KEY_LAST_BUDGET_TIME = "last_budget_time"
        const val KEY_WITNESS_BUDGET = "witness_budget"
        const val KEY_ACCOUNTS_REGISTERED_THIS_INTERVAL = "accounts_registered_this_interval"
        const val KEY_RECENTLY_MISSED_COUNT = "recently_missed_count"
        const val KEY_CURRENT_ASLOT = "current_aslot"
        const val KEY_RECENT_SLOTS_FILLED = "recent_slots_filled"
        const val KEY_DYNAMIC_FLAGS = "dynamic_flags"
        const val KEY_LAST_IRREVERSIBLE_BLOCK_NUM = "last_irreversible_block_num"
    }

}
