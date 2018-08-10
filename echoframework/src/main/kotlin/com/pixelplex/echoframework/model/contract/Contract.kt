package com.pixelplex.echoframework.model.contract

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import com.pixelplex.echoframework.model.AssetAmount
import com.pixelplex.echoframework.model.GrapheneObject
import com.pixelplex.echoframework.model.GrapheneSerializable

/**
 * Represents contract_object from blockchain
 *
 * @author Daria Pechkovskaya
 */
class Contract @JvmOverloads constructor(
    id: String,

    @SerializedName("code")
    var contractCode: String? = null,

    @SerializedName("amount")
    var assetAmount: AssetAmount? = null
) : GrapheneObject(id), GrapheneSerializable {

    override fun toJsonString(): String? = Gson().toJson(this, Contract::class.java)

    override fun toJsonObject(): JsonElement? = null
}
