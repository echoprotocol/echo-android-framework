package org.echo.mobile.framework.model.contract

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Represent log model in Graphene Blockchain
 *
 * @author Dmitriy Bushuev
 */
class Log(val contractType: Int, val rawString: String = "")

/**
 * Base wrapper class for contract result types
 */
sealed class ContractLog(@Expose var address: String) {

    /**
     * Result type for regular contract
     */
    class RegularContractLog(
        address: String,
        @SerializedName("log")
        val calledMethodsHashes: List<String>,
        @Expose
        val data: String,
        @Expose
        @SerializedName("block_num")
        val blockNum: String,
        @Expose
        @SerializedName("trx_num")
        val trxNum: String,
        @Expose
        @SerializedName("op_num")
        val opNum: String
    ) : ContractLog(address)

    /**
     * Result type for x86 contract
     */
    class ContractLogx86(
        address: String,
        @Expose
        val log: String,
        @Expose
        val hash: String,
        @Expose
        val id: String,
        @Expose
        @SerializedName("block_num")
        val blockNum: String,
        @Expose
        @SerializedName("trx_num")
        val trxNum: String,
        @Expose
        @SerializedName("op_num")
        val opNum: String
    ) : ContractLog(address)
}

/**
 * Parses log according to contract type
 */
fun Log.processType(): ContractLog? {
    if (this.contractType == ContractType.REGULAR.ordinal) {
        return Gson().fromJson<ContractLog.RegularContractLog>(
            rawString,
            ContractLog.RegularContractLog::class.java
        )
    }
    if (this.contractType == ContractType.X86.ordinal) {
        return Gson().fromJson<ContractLog.ContractLogx86>(
            rawString,
            ContractLog.ContractLogx86::class.java
        )
    }

    return null
}

/**
 * Converts [Log] to [ContractLog.RegularContractLog] if possible,else returns null
 */
fun Log.toRegular(): ContractLog.RegularContractLog? {
    if (this.contractType == ContractType.REGULAR.ordinal) {
        return Gson().fromJson<ContractLog.RegularContractLog>(
            rawString,
            ContractLog.RegularContractLog::class.java
        )
    }

    return null
}

/**
 * Converts [Log] to [ContractLog.ContractLogx86] if possible,else returns null
 */
fun Log.toX86(): ContractLog.ContractLogx86? {
    if (this.contractType == ContractType.X86.ordinal) {
        return Gson().fromJson<ContractLog.ContractLogx86>(
            rawString,
            ContractLog.ContractLogx86::class.java
        )
    }

    return null
}

