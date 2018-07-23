package com.pixelplex.echolib.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.pixelplex.echolib.model.operations.AccountUpdateOperation
import com.pixelplex.echolib.model.socketoperations.OperationCodingKeys
import java.lang.reflect.Type

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
    val id: String,
    @SerializedName("op")
    @Expose
    val operation: BaseOperation?
) {

    @SerializedName("block_num")
    @Expose
    var blockNum: Long = -1

    @SerializedName("trx_in_block")
    @Expose
    var trxInBlock: Long = -1

    @SerializedName("op_in_trx")
    @Expose
    var opInTrx: Long = -1

    @SerializedName("virtual_op")
    @Expose
    var virtualOp: Long = -1

    /**
     * Json deserializer for [HistoricalTransfer] class
     */
    class HistoryDeserializer : JsonDeserializer<HistoricalTransfer> {

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): HistoricalTransfer {
            val jsonObject = json.asJsonObject

            // Parsing block data information
            val id = jsonObject.get(OperationCodingKeys.ID.key).asString
            val blockNumber = jsonObject.get(BLOCK_NUMBER_KEY).asLong
            val trxNumInBlock = jsonObject.get(TRX_IN_BLOCK_KEY).asLong
            val opNumInTrx = jsonObject.get(OPERATIONS_IN_TRX_KEY).asLong
            val virtualOpNum = jsonObject.get(VIRTUAL_OPERATION_KEY).asLong

            // Parsing operation list
            var operation: BaseOperation? = null

            val jsonOperation = jsonObject.get(OPERATIONS_KEY).asJsonArray
            val operationId = jsonOperation.asJsonArray.get(0).asInt
            if (operationId == OperationType.ACCOUNT_UPDATE_OPERATION.ordinal) {
                operation = context.deserialize<AccountUpdateOperation>(
                    jsonOperation[1],
                    AccountUpdateOperation::class.java
                )
            }

            return HistoricalTransfer(id, operation).apply {
                blockNum = blockNumber
                trxInBlock = trxNumInBlock
                opInTrx = opNumInTrx
                virtualOp = virtualOpNum
            }
        }
    }

    companion object {
        private const val BLOCK_NUMBER_KEY = "block_num"
        private const val TRX_IN_BLOCK_KEY = "trx_in_block"
        private const val OPERATIONS_IN_TRX_KEY = "op_in_trx"
        private const val VIRTUAL_OPERATION_KEY = "virtual_op"

        private const val OPERATIONS_KEY = "op"
    }

}
