package org.echo.mobile.framework.model

import com.google.gson.*
import org.echo.mobile.framework.TIME_DATE_FORMAT
import org.echo.mobile.framework.model.operations.OperationTypeToClassConverter
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Represents Graphene object with operation results of broadcasted transaction
 *
 * @author Daria Pechkovskaya
 */
data class TransactionOperationsResult
/**
 * @param blockData:  Block data instance, containing information about the location of this
 *                    transaction in the blockchain.
 * @param operationsWithResults: The map of operations with results included in this transaction.
 */
    (var blockData: BlockData, var operationsWithResults: Map<BaseOperation, String>) {

    /**
     * Json deserializer for [TransactionOperationsResult] class
     */
    class Deserializer : JsonDeserializer<TransactionOperationsResult> {

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): TransactionOperationsResult {
            val jsonObject = json.asJsonObject

            // Parsing block data information
            val refBlockNum = jsonObject.get(KEY_REF_BLOCK_NUM).asInt
            val refBlockPrefix = jsonObject.get(KEY_REF_BLOCK_PREFIX).asLong
            val dateFormat = SimpleDateFormat(TIME_DATE_FORMAT, Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val expiration = jsonObject.get(KEY_EXPIRATION).asString

            val timeMs = try {
                dateFormat.parse(expiration).time
            } catch (e: ParseException) {
                -1L
            }

            val blockData = BlockData(refBlockNum, refBlockPrefix, timeMs)

            val jsonOperations = jsonObject.get(KEY_OPERATIONS).asJsonArray

            // Parsing operation list
            val operations = parseOperations(jsonOperations, context)

            val jsonResults = jsonObject.get(KEY_OPERATION_RESULTS).asJsonArray

            val operationResultsMap = HashMap<BaseOperation, String>()

            for (i in 0 until jsonResults.size()) {
                val subArray = jsonResults.get(i).asJsonArray

                val resultJson = subArray.get(1)
                val result = if (!resultJson.isJsonPrimitive) "" else resultJson.asString

                operationResultsMap[operations[i]] = result
            }

            return TransactionOperationsResult(blockData, operationResultsMap)
        }

        private fun parseOperations(
            operationsJson: JsonArray,
            context: JsonDeserializationContext
        ) = mutableListOf<BaseOperation>().apply {
            for (i in 0 until operationsJson.size()) {
                val operationJson = operationsJson[i].asJsonArray

                val operationId = operationJson[0].asInt
                val operationBody = operationJson[1]

                val resultType = OperationTypeToClassConverter()
                    .convert(operationId)

                resultType?.let { type ->
                    add(context.deserialize(operationBody, type))
                }
            }
        }

    }

    companion object {
        const val KEY_REF_BLOCK_NUM = "ref_block_num"
        const val KEY_REF_BLOCK_PREFIX = "ref_block_prefix"

        const val KEY_EXPIRATION = "expiration"
        const val KEY_OPERATIONS = "operations"

        const val KEY_OPERATION_RESULTS = "operation_results"
    }
}
