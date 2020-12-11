package org.echo.mobile.framework.model

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import org.echo.mobile.framework.core.mapper.ObjectMapper
import java.lang.reflect.Type

/**
 * Echo sidechain base withdraw model
 *
 * @author Dmitriy Bushuev
 */
sealed class Withdraw(
    id: String
) : GrapheneObject(id) {

    val account: String = ""
    val value: String = ""

    @SerializedName("is_approved")
    val isApproved: Boolean = false

    /**
     * Currently undefined withdraw type
     */
    class Undefined(id: String) : Withdraw(id)

    /**
     * Describes single ethereum withdraw model
     *
     * @author Dmitriy Bushuev
     */
    class EthWithdraw(
        id: String,
        @SerializedName("eth_addr") val address: String = "",
        val approves: List<String> = listOf(),
        @SerializedName("transaction_hash") val transactionHash: String
    ) : Withdraw(id)

    /**
     * Describes single btc withdraw model
     *
     * @author Dmitriy Bushuev
     */
    class BtcWithdraw(
        id: String,
        @SerializedName("btc_addr") val address: String = "",
        @SerializedName("transaction_id") val transactionId: String = ""
    ) : Withdraw(id)

}

/**
 * Deserializer for [Deposit] model
 */
class WithdrawDeserializer : JsonDeserializer<Withdraw> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Withdraw? {
        if (json == null || !json.isJsonObject) return null

        val ethWithdrawal =
            context!!.deserialize<Withdraw.EthWithdraw>(json, Withdraw.EthWithdraw::class.java)

        return ethWithdrawal?.address?.let { ethWithdrawal }
            ?: context.deserialize<Withdraw.BtcWithdraw>(json, Withdraw.BtcWithdraw::class.java)
    }

}

/**
 * Json mapper for [Withdraw] model
 */
class WithdrawMapper : ObjectMapper<Withdraw> {

    override fun map(data: String): Withdraw? =
        data.tryMapWithdraw()

    private fun String.tryMapWithdraw() =
        this.tryMap(Withdraw::class.java)

    private fun <T> String.tryMap(resultType: Class<T>): T? =
        try {
            GsonBuilder().registerTypeAdapter(
                Withdraw::class.java,
                WithdrawDeserializer()
            )
                .create()
                .fromJson(this, resultType)
        } catch (exception: Exception) {
            null
        }

}