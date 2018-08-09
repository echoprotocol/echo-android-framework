package com.pixelplex.echoframework.model.contract

import com.google.gson.annotations.SerializedName

/**
 * Represents contract method
 *
 * @author Daria Pechkovskaya
 */
class ContractMethod(
    @SerializedName("constant")
    var constant: Boolean,

    @SerializedName("inputs")
    var inputParams: ArrayList<ContractMethodParameter> = arrayListOf(),

    @SerializedName("name")
    var name: String = "",

    @SerializedName("outputs")
    var outputParams: ArrayList<ContractMethodParameter> = arrayListOf(),

    @SerializedName("payable")
    var payable: Boolean,

    @SerializedName("type")
    var type: String
)
