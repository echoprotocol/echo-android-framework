package org.echo.mobile.framework.model

import com.google.gson.annotations.SerializedName

/**
 * Represents result of account registration.
 * Example: [{"block_num":114642,"tx_id":"30b2716a2f563b2cfe29442f410681bdae8b6347"}]
 *
 * @author Daria Pechkovskaya
 */
class RegistrationResult (
    @SerializedName("block_num")
    val blockNum: String,

    @SerializedName("tx_id")
    val txId: String
)