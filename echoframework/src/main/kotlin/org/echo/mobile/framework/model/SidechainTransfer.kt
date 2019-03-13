package org.echo.mobile.framework.model

import com.google.gson.annotations.SerializedName

/**
 * Describes sidechain transfer model
 *
 * @author Dmitriy Bushuev
 */
class SidechainTransfer(
    id: String,
    @SerializedName("transfer_id")
    val transferId: Long,
    val receiver: String,
    val signatures: String,
    @SerializedName("withdraw_code")
    val withdrawCode: String
) : GrapheneObject(id)