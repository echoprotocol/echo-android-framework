package com.pixelplex.echolib.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.math.BigInteger

/**
 * Represents balance model in Graphene blockchain
 *
 * @author Dmitriy Bushuev
 */
data class Balance(
    @SerializedName("id") @Expose var id: String,
    @SerializedName("owner") @Expose var owner: String,
    @SerializedName("asset_type") @Expose var assetType: String,
    @SerializedName("balance") @Expose var balance: BigInteger
)
