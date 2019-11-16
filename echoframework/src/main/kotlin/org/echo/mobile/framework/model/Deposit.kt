package org.echo.mobile.framework.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.echo.mobile.framework.core.mapper.ObjectMapper

/**
 * Echo sidechain base deposit model
 *
 * @author Dmitriy Bushuev
 */
sealed class Deposit(
    id: String
) : GrapheneObject(id) {
    val account: String = ""
    val value: String = ""
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
        @SerializedName("deposit_id") val depositId: String
    ) : Deposit(id)

    /**
     * Describes single btc deposit model
     */
    class BtcDeposit(
        id: String,
        @SerializedName("intermediate_deposit_id") val depositId: String,
        @SerializedName("block_number") val blockNumber: String,
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
 * Json mapper for [Deposit] model
 */
class DepositMapper : ObjectMapper<Deposit> {

    override fun map(data: String): Deposit? =
        data.tryMapDeposit()

    private fun String.tryMapDeposit() =
        this.tryMap(Deposit.EthDeposit::class.java) ?: this.tryMap(Deposit.BtcDeposit::class.java)

    private fun <T> String.tryMap(resultType: Class<T>): T? =
        try {
            Gson().fromJson(this, resultType)
        } catch (exception: Exception) {
            null
        }

}

