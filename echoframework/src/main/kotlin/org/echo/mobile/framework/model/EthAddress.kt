package org.echo.mobile.framework.model

import com.google.gson.annotations.SerializedName

/**
 * Describes account'sethereum address model
 *
 * @author Dmitriy Bushuev
 */
data class EthAddress(
    @SerializedName("acc_id") val accountId: String,
    @SerializedName("eth_addr") val address: String,
    @SerializedName("is_approved") val isApproved: Boolean,
    val approves: List<String>
)