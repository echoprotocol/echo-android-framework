package com.pixelplex.echolib.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Represents account history
 * <a href="https://bitshares.org/doxygen/classgraphene_1_1chain_1_1operation__history__object.html">Source</a>
 * <p>
 *     Contains required information about user operations
 * </p>
 *
 * @author Daria Pechkovskaya
 */
data class HistoricalTransfer(
    @Expose val id: String,
    @SerializedName("op") @Expose val operation: BaseOperation,
    @Expose val result: Result,
    @SerializedName("block_num") @Expose val blockNum: Long,
    @SerializedName("trx_in_block") @Expose val trxInBlock: Long,
    @SerializedName("op_in_trx") @Expose val opInTrx: Long,
    @SerializedName("virtual_op") @Expose val virtualOp: Long
)
