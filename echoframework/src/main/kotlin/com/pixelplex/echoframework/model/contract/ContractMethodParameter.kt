package com.pixelplex.echoframework.model.contract

import com.google.gson.annotations.Expose
import java.io.Serializable

/**
 * Model of method parameter for contract
 *
 * @author Daria Pechkovskaya
 */
class ContractMethodParameter
@JvmOverloads constructor(
    @Expose
    var name: String,

    @Expose
    var type: String,

    var value: String = ""

) : Serializable {

    companion object {
        const val TYPE_BOOL = "bool"

        const val TYPE_UINT = "uint"
        const val TYPE_UINT8 = "uint8"
        const val TYPE_UINT16 = "uint16"
        const val TYPE_UINT32 = "uint32"
        const val TYPE_UINT64 = "uint64"
        const val TYPE_UINT128 = "uint128"
        const val TYPE_UINT256 = "uint256"
        const val TYPE_ADDRESS = "address"

        const val TYPE_INT = "int"
    }
}
