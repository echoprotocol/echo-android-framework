package org.echo.mobile.framework.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.echo.mobile.framework.core.mapper.ObjectMapper

/**
 * Describes single ethereum withdraw model
 *
 * @author Dmitriy Bushuev
 */
class EthWithdraw(
    id: String,
    val account: String = "",
    @SerializedName("eth_addr") val address: String = "",
    val value: String = "",
    @SerializedName("is_approved") val isApproved: Boolean = false,
    val approves: List<String> = listOf()
) : GrapheneObject(id)

/**
 * Json mapper for [EthWithdraw] model
 */
class EthWithdrawMapper : ObjectMapper<EthWithdraw> {
    private val gson = Gson()

    override fun map(data: String): EthWithdraw? =
        gson.fromJson<EthWithdraw>(data, EthWithdraw::class.java)

}