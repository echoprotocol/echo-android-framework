package org.echo.mobile.framework.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.echo.mobile.framework.model.operations.OperationTypeToClassConverter
import org.echo.mobile.framework.model.socketoperations.OperationCodingKeys
import java.lang.reflect.Type
import java.util.Date

/**
 * Represents account history
 * [History object details](https://dev-doc.myecho.app/classgraphene_1_1chain_1_1operation__history__object.html)
 *
 * Contains required information about user operations
 *
 * @author Daria Pechkovskaya
 * @author Dmitriy Bushuev
 */
data class HistoricalTransfer(
    val id: String,
    @SerializedName(OPERATIONS_KEY)
    @Expose
    val operation: BaseOperation?,
    var timestamp: Date? = null
) {

    @SerializedName(BLOCK_NUMBER_KEY)
    @Expose
    var blockNum: Long = -1

    @SerializedName(TRX_IN_BLOCK_KEY)
    @Expose
    var trxInBlock: Long = -1

    @SerializedName(OPERATIONS_IN_TRX_KEY)
    @Expose
    var opInTrx: Long = -1

    @SerializedName(VIRTUAL_OPERATION_KEY)
    @Expose
    var virtualOp: Long = -1

    var result: HistoryResult? = null

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
            val operation = parseOperation(jsonObject.get(OPERATIONS_KEY), context)

            val result = jsonObject.get(OPERATION_RESULT_KEY).asJsonArray

            val resultId = result[0].asInt
            var objectId: String? = null
            var payload: Any? = null

            if (resultId == 1) {
                objectId = result[1].asString
            } else {
                payload = result[1]
            }

            return HistoricalTransfer(id, operation).apply {
                this.blockNum = blockNumber
                this.trxInBlock = trxNumInBlock
                this.opInTrx = opNumInTrx
                this.virtualOp = virtualOpNum
                this.result = HistoryResult(resultId, objectId, payload)
            }
        }

        private fun parseOperation(
            operationJson: JsonElement,
            context: JsonDeserializationContext
        ): BaseOperation? {
            val jsonOperation = operationJson.asJsonArray
            val operationId = jsonOperation.asJsonArray.get(0).asInt
            val operationBody = jsonOperation[1]

            val resultType = OperationTypeToClassConverter()
                .convert(operationId)

            return resultType?.let { type ->
                context.deserialize(
                    operationBody,
                    type
                )
            }
        }
    }

    companion object {
        private const val BLOCK_NUMBER_KEY = "block_num"
        private const val TRX_IN_BLOCK_KEY = "trx_in_block"
        private const val OPERATIONS_IN_TRX_KEY = "op_in_trx"
        private const val VIRTUAL_OPERATION_KEY = "virtual_op"
        private const val OPERATION_RESULT_KEY = "result"

        private const val OPERATIONS_KEY = "op"
    }

}

