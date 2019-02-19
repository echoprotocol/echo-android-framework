package org.echo.mobile.framework.model.contract

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.echo.mobile.framework.model.Log

/**
 * Contains classes, describing result of contract operation
 *
 * @author Daria Pechkovskaya
 */
class ContractResult(val contractType: Int, val rawString: String = "")

/**
 * Converts [ContractResult] to [RegularContractResult] if possible,else returns null
 */
fun ContractResult.toRegular(): RegularContractResult? {
    if (this.contractType == ContractType.REGULAR.ordinal) {
        return Gson().fromJson<RegularContractResult>(rawString, RegularContractResult::class.java)
    }

    return null
}

/**
 * Converts [ContractResult] to [ContractResultx86] if possible,else returns null
 */
fun ContractResult.toX86(): ContractResultx86? {
    if (this.contractType == ContractType.X86.ordinal) {
        return Gson().fromJson<ContractResultx86>(rawString, ContractResultx86::class.java)
    }

    return null
}

/**
 * Contains all possible contract types in blockchain
 */
enum class ContractType {
    REGULAR,
    X86
}

/**
 * Model of contract operation result for x86 VM
 */
class ContractResultx86(
    @SerializedName("output")
    val output: String
)

/**
 * Model of contract operation result
 */
class RegularContractResult(
    @SerializedName("exec_res")
    val execRes: ExecRes,

    @SerializedName("tr_receipt")
    val trReceipt: TrReceipt

)

/**
 * Information about operation executing
 */
class ExecRes(
    @Expose
    val excepted: String = "",

    @SerializedName("new_address")
    val newAddress: String = "",

    @Expose
    val output: String = "",

    @SerializedName("code_deposit")
    val codeDeposit: String = "",

    @SerializedName("gas_refunded")
    val gasRefunded: String = "",

    @SerializedName("deposit_size")
    val depositSize: Long = 0,

    @SerializedName("gas_for_deposit")
    val gasForDeposit: String = ""

)

/**
 * Information about transaction
 */
class TrReceipt(
    @SerializedName("status_code")
    val statusCode: String = "",

    @SerializedName("gas_used")
    val gasUsed: String = "",

    @Expose
    val bloom: String = "",

    @Expose
    val log: List<Log> = listOf()
)
