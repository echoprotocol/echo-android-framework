package com.pixelplex.echoframework.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.math.BigInteger

/**
 * Represents balance model in Graphene blockchain
 *
 * @author Dmitriy Bushuev
 */
data class Balance(
    @Expose var id: String,
    @Expose var owner: String,
    @SerializedName("asset_type") @Expose var assetType: String,
    @Expose var balance: BigInteger
)
