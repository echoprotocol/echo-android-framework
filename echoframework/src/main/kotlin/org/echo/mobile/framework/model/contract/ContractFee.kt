package org.echo.mobile.framework.model.contract

import com.google.gson.annotations.SerializedName
import org.echo.mobile.framework.model.AssetAmount

/**
 * Represents model of contract call fee
 *
 * @author Dmitriy Bushuev
 */
class ContractFee(
    val fee: AssetAmount,
    @SerializedName("user_to_pay") val feeToPay: AssetAmount
)

