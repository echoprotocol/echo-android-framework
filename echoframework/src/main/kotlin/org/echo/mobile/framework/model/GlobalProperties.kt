package org.echo.mobile.framework.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import org.echo.mobile.framework.model.FeeParameter.Companion.BASIC_FEE_KEY
import org.echo.mobile.framework.model.FeeParameter.Companion.LONG_SYMBOL_KEY
import org.echo.mobile.framework.model.FeeParameter.Companion.PREMIUM_FEE_KEY
import org.echo.mobile.framework.model.FeeParameter.Companion.PRICE_PER_KEY_BYTES_KEY
import org.echo.mobile.framework.model.FeeParameter.Companion.SYMBOL3_KEY
import org.echo.mobile.framework.model.FeeParameter.Companion.SYMBOL4_KEY
import java.lang.reflect.Type

/**
 * Contains all global blockchain properties
 *
 * @author Dmitriy Bushuev
 */
class GlobalProperties {

    val id: String = ""

    var parameters: Parameters? = null

    @SerializedName("active_committee_members")
    private val activeCommitteeMembers: List<String>? = null

    @SerializedName("active_witnesses")
    private val activeWitnesses: List<String>? = null
}

/**
 * Blockchain global properties parameters list
 */
class Parameters {

    @SerializedName("maximum_asset_feed_publishers")
    private val maximumAssetFeedPublishers: String? = null
    @SerializedName("maximum_witness_count")
    private val maximumWitnessCount: String? = null
    @SerializedName("maximum_asset_whitelist_authorities")
    private val maximumAssetWhitelistAuthorities: String? = null
    @SerializedName("maximum_proposal_lifetime")
    private val maximumProposalLifetime: String? = null
    @SerializedName("witness_pay_per_block")
    private val witnessPayPerBlock: String? = null
    @SerializedName("block_interval")
    private val blockInterval: String? = null
    @SerializedName("maintenance_skip_slots")
    private val maintenanceSkipSlots: String? = null
    @SerializedName("maximum_time_until_expiration")
    private val maximumTimeUntilExpiration: String? = null
    @SerializedName("max_predicate_opcode")
    private val maxPredicateOpcode: String? = null
    @SerializedName("echorand_config")
    private val echorandConfig: EchorandConfig? = null
    @SerializedName("reserve_percent_of_fee")
    private val reservePercentOfFee: String? = null
    @SerializedName("max_authority_depth")
    private val maxAuthorityDepth: String? = null
    @SerializedName("committee_proposal_review_period")
    private val committeeProposalReviewPeriod: String? = null
    @SerializedName("account_fee_scale_bitshifts")
    private val accountFeeScaleBitshifts: String? = null
    @SerializedName("sidechain_config")
    private val sidechainConfig: SidechainConfig? = null
    @SerializedName("count_non_member_votes")
    private val countNonMemberVotes: String? = null
    @SerializedName("gas_price")
    private val gasPrice: GasPrice? = null
    @SerializedName("fee_liquidation_threshold")
    private val feeLiquidationThreshold: String? = null
    @SerializedName("maximum_transaction_size")
    private val maximumTransactionSize: String? = null
    @SerializedName("maximum_block_size")
    private val maximumBlockSize: String? = null
    @SerializedName("worker_budget_per_day")
    private val workerBudgetPerDay: String? = null
    @SerializedName("maximum_authority_membership")
    private val maximumAuthorityMembership: String? = null
    @SerializedName("maximum_committee_count")
    private val maximumCommitteeCount: String? = null
    @SerializedName("accounts_per_fee_scale")
    private val accountsPerFeeScale: String? = null
    @SerializedName("allow_non_member_whitelists")
    private val allowNonMemberWhitelists: String? = null
    @SerializedName("extensions")
    private val extensions: Array<String>? = null
    @SerializedName("cashback_vesting_threshold")
    private val cashbackVestingThreshold: String? = null
    @SerializedName("current_fees")
    private val currentFees: CurrentFees? = null
    @SerializedName("cashback_vesting_period_seconds")
    private val cashbackVestingPeriodSeconds: String? = null
    @SerializedName("maintenance_interval")
    private val maintenanceInterval: String? = null
    @SerializedName("network_percent_of_fee")
    private val networkPercentOfFee: String? = null
    @SerializedName("lifetime_referrer_percent_of_fee")
    private val lifetimeReferrerPercentOfFee: String? = null

}

/**
 * Describes fees that exist in current blockchain version
 */
class CurrentFees {

    var scale: String? = null

    var parameters: FeeParameters? = null

}

/**
 * List fo all blockchain fees
 */
class FeeParameters(var parameters: List<FeeParameter>? = null) {

    /**
     * Json deserializer for [FeeParameters] class objects
     */
    class Deserializer : JsonDeserializer<FeeParameters> {

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): FeeParameters? {
            if (json == null || !json.isJsonArray) return null

            val jsonAccount = json.asJsonArray

            val feeParams = mutableListOf<FeeParameter>()

            jsonAccount.forEach { element ->
                val itemArray = element.asJsonArray

                val type = itemArray[0]?.asInt ?: -1
                val payload = itemArray[1].asJsonObject

                val fee = payload.get("fee")?.asLong
                val pricePerKbyte = payload.get(PRICE_PER_KEY_BYTES_KEY)?.asLong
                val premiumFee = payload.get(PREMIUM_FEE_KEY)?.asLong
                val basicFee = payload.get(BASIC_FEE_KEY)?.asLong
                val symbol3 = payload.get(SYMBOL3_KEY)?.asLong
                val symbol4 = payload.get(SYMBOL4_KEY)?.asLong
                val longSymbol = payload.get(LONG_SYMBOL_KEY)?.asLong

                feeParams.add(
                    FeeParameter(
                        type,
                        pricePerKbyte,
                        premiumFee,
                        fee,
                        basicFee,
                        symbol3,
                        symbol4,
                        longSymbol
                    )
                )
            }

            return FeeParameters(feeParams)
        }
    }

}

/**
 * Blockchain single fee item
 */
class FeeParameter(
    var type: Int = -1,
    @SerializedName(PRICE_PER_KEY_BYTES_KEY)
    var pricePerKbyte: Long? = null,
    @SerializedName(PREMIUM_FEE_KEY)
    var premiumFee: Long? = null,
    @SerializedName(FEE_KEY)
    var fee: Long? = null,
    @SerializedName(BASIC_FEE_KEY)
    var basicFee: Long? = null,
    @SerializedName(SYMBOL3_KEY)
    var symbol3: Long? = null,
    @SerializedName(SYMBOL4_KEY)
    var symbol4: Long? = null,
    @SerializedName(LONG_SYMBOL_KEY)
    var longSymbol: Long? = null
) {
    companion object {
        const val PRICE_PER_KEY_BYTES_KEY = "price_per_kbyte"
        const val PREMIUM_FEE_KEY = "premium_fee"
        const val FEE_KEY = "fee"
        const val BASIC_FEE_KEY = "basic_fee"
        const val SYMBOL3_KEY = "symbol3"
        const val SYMBOL4_KEY = "symbol4"
        const val LONG_SYMBOL_KEY = "long_symbol"
    }
}

/**
 * Describes blockchain gas price
 */
class GasPrice {
    @SerializedName("gas_amount")
    var gasAmount: String? = null
    @SerializedName("price")
    var price: String? = null

}

/**
 * Describes all sidechain's configs
 */
class SidechainConfig {
    @SerializedName("echo_transfer_ready_topic")
    var echoTransferReadyTopic: String? = null
    @SerializedName("echo_vote_method")
    var echoVoteMethod: String? = null
    @SerializedName("echo_sign_method")
    var echoSignMethod: String? = null
    @SerializedName("echo_contract_id")
    var echoContractId: String? = null
    @SerializedName("eth_transfer_topic")
    var ethTransferTopic: String? = null
    @SerializedName("eth_committee_method")
    var ethCommitteeMethod: String? = null
    @SerializedName("echo_transfer_topic")
    var echoTransferTopic: String? = null
    @SerializedName("eth_contract_address")
    var ethContractAddress: String? = null

}

/**
 * Describes all echorand's configs
 */
class EchorandConfig {
    @SerializedName("_verifier_count")
    var verifierCount: String? = null
    @SerializedName("_time_net_1mb")
    var timeNet1mb: String? = null
    @SerializedName("_gc1_delay")
    var gc1Delay: String? = null
    @SerializedName("_time_net_256b")
    var timeNet256b: String? = null
    @SerializedName("_ok_threshold")
    var okThreshold: String? = null
    @SerializedName("_max_bba_steps")
    var maxBbaSteps: String? = null
    @SerializedName("_creator_count")
    var creatorCount: String? = null

}
