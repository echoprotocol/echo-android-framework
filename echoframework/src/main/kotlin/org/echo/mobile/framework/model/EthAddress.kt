package org.echo.mobile.framework.model

import com.google.gson.annotations.SerializedName

/**
 * Describes account'sethereum address model
 *
 * @author Dmitriy Bushuev
 */
data class EthAddress(
    val id: String,
    @SerializedName("account") val accountId: String,
    @SerializedName("eth_addr") val address: String,
    @SerializedName("is_approved") val isApproved: Boolean,
    val approves: List<String>
)