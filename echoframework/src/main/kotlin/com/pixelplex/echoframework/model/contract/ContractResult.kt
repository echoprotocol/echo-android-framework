package com.pixelplex.echoframework.model.contract

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Contains classes, describing result of contract operation
 *
 * @author Daria Pechkovskaya
 */

/**
 * Model of contract operation result
 */
class ContractResult(
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
    val log: List<String> = listOf()
)
