package org.echo.mobile.framework.model

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import org.echo.mobile.framework.core.mapper.ObjectMapper
import java.lang.reflect.Type

/**
 * Echo sidechain base deposit model
 *
 * @author Dmitriy Bushuev
 */
sealed class Deposit(
    id: String
) : GrapheneObject(id) {
    val account: String = ""

    @SerializedName("is_approved")
    val isApproved: Boolean = false
    val approves: List<String> = listOf()

    /**
     * Currently undefined deposit type
     */
    class Undefined(id: String) : Deposit(id)

    /**
     * Describes single ethereum deposit model
     */
    class EthDeposit(
        id: String,
        @SerializedName("deposit_id") val depositId: String,
        val value: String = "",
        @SerializedName("transaction_hash") val transactionHash: String = ""
    ) : Deposit(id)

    /**
     * Describes single btc deposit model
     */
    class BtcDeposit(
        id: String,
        @SerializedName("intermediate_deposit_id") val depositId: String,
        @SerializedName("tx_info") val transactionInfo: TransactionInfo
    ) : Deposit(id)

    /**
     * Describes transaction info in btc deposit
     */
    class TransactionInfo(
        @SerializedName("block_number") val blockNumber: String,
        val out: TransactionOut
    )

    /**
     * Describes transaction out in btc deposit
     */
    class TransactionOut(
        @SerializedName("tx_id") val transactionId: String,
        val index: String,
        val amount: String
    )

}

/**
 * Deserializer for [Deposit] model
 */
class DepositDeserializer : JsonDeserializer<Deposit> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Deposit? {
        if (json == null || !json.isJsonObject) return null

        val ethWithdrawal =
            context!!.deserialize<Deposit.EthDeposit>(json, Deposit.EthDeposit::class.java)

        return ethWithdrawal?.depositId?.let { ethWithdrawal }
            ?: context.deserialize<Deposit.BtcDeposit>(json, Deposit.BtcDeposit::class.java)
    }

}

/**
 * Json mapper for [Withdraw] model
 */
class DepositMapper : ObjectMapper<Deposit> {

    override fun map(data: String): Deposit? =
        data.tryMapWithdraw()

    private fun String.tryMapWithdraw() =
        this.tryMap(Deposit::class.java)

    private fun <T> String.tryMap(resultType: Class<T>): T? =
        try {
            GsonBuilder().registerTypeAdapter(
                Deposit::class.java,
                DepositDeserializer()
            )
                .create()
                .fromJson(this, resultType)
        } catch (exception: Exception) {
            null
        }

}

