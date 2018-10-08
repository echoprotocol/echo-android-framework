package org.echo.mobile.framework.model.contract

import com.google.gson.annotations.SerializedName

/**
 * Represents contract_struct from blockchain
 *
 * @author Daria Pechkovskaya
 */
class ContractStruct(
    @SerializedName(KEY_CONTRACT_INFO)
    val contractInfo: ContractInfo,

    @SerializedName(KEY_CODE)
    val code: String = "",

    @SerializedName(KEY_STORAGE)
    val storage: Map<String, String> = mapOf()
) {

    companion object {
        const val KEY_CONTRACT_INFO = "contract_info"
        const val KEY_CODE = "code"
        const val KEY_STORAGE = "storage"
    }
}
