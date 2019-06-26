package org.echo.mobile.framework.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.echo.mobile.framework.core.mapper.ObjectMapper

/**
 * Describes single ethereum deposit model
 *
 * @author Dmitriy Bushuev
 */
class EthDeposit(
    id: String,
    val account: String = "",
    val value: String = "",
    @SerializedName("is_approved") val isApproved: Boolean = false,
    val approves: List<String> = listOf()
) : GrapheneObject(id)

/**
 * Json mapper for [EthDeposit] model
 */
class EthDepositMapper : ObjectMapper<EthDeposit> {
    private val gson = Gson()

    override fun map(data: String): EthDeposit? =
        gson.fromJson<EthDeposit>(data, EthDeposit::class.java)

}