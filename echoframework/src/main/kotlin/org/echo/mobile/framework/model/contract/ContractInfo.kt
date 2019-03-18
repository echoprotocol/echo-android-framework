package org.echo.mobile.framework.model.contract

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.echo.mobile.framework.model.GrapheneObject

/**
 * Represents info object about contract from blockchain
 *
 * @author Daria Pechkovskaya
 */
class ContractInfo(
    id: String,
    @Expose
    val statistics: String,
    @Expose
    val destroyed: Boolean,
    @Expose
    @SerializedName("type")
    val contractType: String = "",
    @Expose
    @SerializedName("supported_asset_id")
    val supported_asset_id: String = ""
) : GrapheneObject(id) {

    override fun toString(): String =
        "${javaClass.simpleName}(id=$id, statistics=$statistics, destroyed=$destroyed)"
}
