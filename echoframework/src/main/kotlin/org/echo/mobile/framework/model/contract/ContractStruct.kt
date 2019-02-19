package org.echo.mobile.framework.model.contract

import com.google.gson.annotations.SerializedName

/**
 * Represents contract_struct from blockchain
 *
 * @author Daria Pechkovskaya
 */
class ContractStruct(
    val contractType: Int,

    @SerializedName(KEY_CODE)
    val code: String = ""
) {

    companion object {
        const val KEY_CODE = "code"
    }
}
