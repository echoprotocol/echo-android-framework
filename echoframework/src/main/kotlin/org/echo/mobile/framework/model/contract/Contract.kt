package org.echo.mobile.framework.model.contract

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.GrapheneObject
import org.echo.mobile.framework.model.GrapheneSerializable

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
