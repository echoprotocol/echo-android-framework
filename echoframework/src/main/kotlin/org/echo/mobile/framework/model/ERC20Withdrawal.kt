package org.echo.mobile.framework.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.echo.mobile.framework.core.mapper.ObjectMapper

/**
 * Echo sidechain withdrawal model
 *
 * @author Dmitriy Bushuev
 */
class ERC20Withdrawal(
    id: String,
    @SerializedName("withdraw_id") val withdrawId: String = "",
    val account: String = "",
    val to: String = "",
    val value: String = "",
    @SerializedName("is_approved") val isApproved: Boolean = false,
    val approves: List<String> = listOf(),
    @SerializedName("erc20_token") val erc20Token: String = "",
    @SerializedName("transaction_hash") val transactionHash: String = ""
) : GrapheneObject(id)

/**
 * Json mapper for [ERC20Withdrawal] model
 */
class Erc20WithdrawalMapper : ObjectMapper<ERC20Withdrawal> {

    override fun map(data: String): ERC20Withdrawal? =
        try {
            Gson().fromJson(data, ERC20Withdrawal::class.java)
        } catch (exception: Exception) {
            null
        }

}