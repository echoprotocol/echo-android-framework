package com.pixelplex.echoframework.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * @author Daria Pechkovskaya
 */
class FullAccount(
    @Expose var account: Account? = null,
    @SerializedName("registrar_name") @Expose var registrarName: String? = null,
    @SerializedName("referrer_name") @Expose var referrerName: String? = null,
    @Expose var balances: List<Balance>? = null,
    @Expose var assets: List<String>? = null
)
