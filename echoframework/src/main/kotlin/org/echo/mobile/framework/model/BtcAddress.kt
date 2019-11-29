package org.echo.mobile.framework.model

import com.google.gson.annotations.SerializedName

/**
 * Describes account's botcoin address model
 *
 * @author Dmitriy Bushuev
 */
data class BtcAddress(
    val id: String,
    @SerializedName("account") val accountId: String,
    @SerializedName("is_relevant") val isRelevant: Boolean,
    @SerializedName("backup_address") val backupAddress: String,
    @SerializedName("deposit_address") val depositAddress: DepositAddress,
    @SerializedName("committee_member_ids_in_script") val committeeMemberIdsInScript: List<List<String>>
)

/**
 * Describes deposit bitcoin address
 */
data class DepositAddress(val address: String)