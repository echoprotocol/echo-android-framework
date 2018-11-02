package org.echo.mobile.framework.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Represents Graphene notice object for broadcasted transaction
 *
 * @author Daria Pechkovskaya
 */
data class TransactionResult(
    @Expose
    val id: String,

    @SerializedName("block_num")
    val blockNum: String,

    @SerializedName("trx_num")
    val trxNum: Long,

    @Expose
    val trx: TransactionOperationsResult
)

