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
    val activeCommitteeMembers: List<List<String>>? = null

    @SerializedName("active_witnesses")
    val activeWitnesses: List<String>? = null
}

/**
 * Blockchain global properties parameters list
 */
class Parameters {
    @SerializedName("maximum_asset_feed_publishers")
    val maximumAssetFeedPublishers: String? = null
    @SerializedName("maximum_witness_count")
    val maximumWitnessCount: String? = null
    @SerializedName("maximum_asset_whitelist_authorities")
    val maximumAssetWhitelistAuthorities: String? = null
    @SerializedName("maximum_proposal_lifetime")
    val maximumProposalLifetime: String? = null
    @SerializedName("witness_pay_per_block")
    val witnessPayPerBlock: String? = null
    @SerializedName("maximum_time_until_expiration")
    val maximumTimeUntilExpiration: String? = null
    @SerializedName("echorand_config")
    val echorandConfig: EchorandConfig? = null
    @SerializedName("max_authority_depth")
    val maxAuthorityDepth: String? = null
    @SerializedName("committee_proposal_review_period")
    val committeeProposalReviewPeriod: String? = null
    @SerializedName("sidechain_config")
    val sidechainConfig: SidechainConfig? = null
    @SerializedName("gas_price")
    val gasPrice: GasPrice? = null
    @SerializedName("maximum_transaction_size")
    val maximumTransactionSize: String? = null
    @SerializedName("maximum_block_size")
    val maximumBlockSize: String? = null
    @SerializedName("worker_budget_per_day")
    val workerBudgetPerDay: String? = null
    @SerializedName("maximum_authority_membership")
    val maximumAuthorityMembership: String? = null
    @SerializedName("extensions")
    val extensions: Array<String>? = null
    @SerializedName("current_fees")
    val currentFees: CurrentFees? = null
    @SerializedName("maintenance_interval")
    val maintenanceInterval: String? = null
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
    @SerializedName("eth_contract_address")
    var ethContractAddress: String? = null
    @SerializedName("eth_committee_update_method")
    var ethCommitteeUpdateMethod: Method? = null
    @SerializedName("eth_gen_address_method")
    var echoGenAddressMethod: Method? = null
    @SerializedName("eth_withdraw_method")
    var ethWithdrawMethod: Method? = null
    @SerializedName("eth_update_addr_method")
    var ethUpdateAddressMethod: Method? = null
    @SerializedName("ETH_asset_id")
    var ethAssetId: String? = null
    @SerializedName("BTC_asset_id")
    var btcAssetId: String? = null
    @SerializedName("waiting_eth_blocks")
    var waitingETHBlocks: String? = null
    @SerializedName("eth_deposit_topic")
    var ethDepositTopic: String? = null
    @SerializedName("eth_gen_address_topic")
    var ethGenerateAddressTopic: String? = null
    @SerializedName("eth_withdraw_topic")
    var ethWithdrawTopic: String? = null
    @SerializedName("eth_committee_updated_topic")
    var ethCommitteeUpdateTopic: String? = null
    @SerializedName("erc20_withdraw_topic")
    var erc20WithdrawTopic: String? = null
    @SerializedName("gas_price")
    var gasPrice: String? = null
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

/**
 * Describes sidechain method informationstructure
 */
class Method {
    var method: String? = null
    var gas: Long? = null
}
