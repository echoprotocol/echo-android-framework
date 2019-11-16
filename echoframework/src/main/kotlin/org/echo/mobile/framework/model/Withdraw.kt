package org.echo.mobile.framework.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.echo.mobile.framework.core.mapper.ObjectMapper

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
    val approves: List<String> = listOf()

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
        @SerializedName("eth_addr") val address: String = ""
    ) : Withdraw(id)

    /**
     * Describes single btc withdraw model
     *
     * @author Dmitriy Bushuev
     */
    class BtcWithdraw(
        id: String,
        @SerializedName("btc_addr") val address: String = ""
    ) : Withdraw(id)

}

/**
 * Json mapper for [Withdraw] model
 */
class WithdrawMapper : ObjectMapper<Withdraw> {

    override fun map(data: String): Withdraw? =
        data.tryMapWithdraw()

    private fun String.tryMapWithdraw() =
        this.tryMap(Withdraw.EthWithdraw::class.java)
            ?: this.tryMap(Withdraw.BtcWithdraw::class.java)

    private fun <T> String.tryMap(resultType: Class<T>): T? =
        try {
            Gson().fromJson(this, resultType)
        } catch (exception: Exception) {
            null
        }

}

/**
 * Json mapper for [EthWithdraw] model
 */
class EthWithdrawMapper : ObjectMapper<Withdraw.EthWithdraw> {
    private val gson = Gson()

    override fun map(data: String): Withdraw.EthWithdraw? =
        gson.fromJson<Withdraw.EthWithdraw>(data, Withdraw.EthWithdraw::class.java)

}