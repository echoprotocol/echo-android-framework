package com.pixelplex.echoframework.model

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.pixelplex.echoframework.TIME_DATE_FORMAT
import java.io.Serializable
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Represents account model in Graphene blockchain
 * (@see https://bitshares.org/doxygen/classgraphene_1_1chain_1_1dynamic__global__property__object.html)
 *
 * @author Daria Pechkovskaya
 */
class DynamicGlobalProperties(
    id: String,
    @SerializedName(KEY_HEAD_BLOCK_NUMBER) @Expose val headBlockNumber: Long = 0,
    @SerializedName(KEY_HEAD_BLOCK_ID) @Expose val headBlockId: String,
    var date: Date?,
    @SerializedName(KEY_CURRENT_WITNESS) @Expose val currentWitness: String,
    var nextMaintenanceDate: Date?,
    @SerializedName(KEY_LAST_BUDGET_TIME) @Expose val lastBudgetTime: String,
    @SerializedName(KEY_WITNESS_BUDGET) @Expose val witnessBudget: Long = 0,
    @SerializedName(KEY_ACCOUNTS_REGISTERED_THIS_INTERVAL) @Expose val accountsRegisteredThisInterval: Long = 0,
    @SerializedName(KEY_RECENTLY_MISSED_COUNT) @Expose val recentlyMissedCount: Long = 0,
    @SerializedName(KEY_CURRENT_ASLOT) @Expose val currentAslot: Long = 0,
    @SerializedName(KEY_RECENT_SLOTS_FILLED) @Expose val recentSlotsFilled: String,
    @SerializedName(KEY_DYNAMIC_FLAGS) @Expose val dynamicFlags: Int = 0,
    @SerializedName(KEY_LAST_IRREVERSIBLE_BLOCK_NUM) @Expose val lastIrreversibleBlockNum: Long = 0

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

    /**
     * Class that will parse the JSON element containing the dynamic global properties object and
     * return an instance of the [DynamicGlobalProperties] class.
     */
    class Deserializer : JsonDeserializer<DynamicGlobalProperties> {

        override fun deserialize(
            jsonElement: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): DynamicGlobalProperties? {

            if (jsonElement == null || !jsonElement.isJsonObject) {
                return null
            }

            val jsonObject = jsonElement.asJsonObject

            val dynamicGlobalProperties = Gson().fromJson<DynamicGlobalProperties>(
                jsonElement,
                DynamicGlobalProperties::class.java
            )

            val dateFormat = SimpleDateFormat(TIME_DATE_FORMAT, Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            dynamicGlobalProperties.date = try {
                dateFormat.parse(jsonObject.get(DynamicGlobalProperties.KEY_TIME).asString)
            } catch (e: ParseException) {
                println("ParseException. Msg: " + e.message)
                null
            }

            dynamicGlobalProperties.nextMaintenanceDate = try {
                dateFormat.parse(jsonObject.get(DynamicGlobalProperties.KEY_NEXT_MAINTENANCE_TIME).asString)
            } catch (e: ParseException) {
                println("ParseException. Msg: " + e.message)
                null
            }

            return dynamicGlobalProperties
        }
    }

}
