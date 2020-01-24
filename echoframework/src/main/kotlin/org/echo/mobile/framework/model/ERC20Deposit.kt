package org.echo.mobile.framework.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.echo.mobile.framework.core.mapper.ObjectMapper

/**
 * Echo erc20 sidechain deposit model
 *
 * @author Dmitriy Bushuev
 */
class ERC20Deposit(
    id: String,
    val account: String = "",
    val value: String = "",
    @SerializedName("is_approved") val isApproved: Boolean = false,
    val approves: List<String> = listOf(),
    @SerializedName("transaction_hash") val transactionHash: String = "",
    @SerializedName("erc20_addr") val erc20Address: String = ""
) : GrapheneObject(id)

/**
 * Json mapper for [Deposit] model
 */
class Erc20DepositMapper : ObjectMapper<ERC20Deposit> {

    override fun map(data: String): ERC20Deposit? =
        try {
            Gson().fromJson(data, ERC20Deposit::class.java)
        } catch (exception: Exception) {
            null
        }

}
