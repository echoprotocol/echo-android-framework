package org.echo.mobile.framework.model

import com.google.gson.*
import org.echo.mobile.framework.TIME_DATE_FORMAT
import org.echo.mobile.framework.model.operations.OperationTypeToClassConverter
import org.echo.mobile.framework.support.format
import org.spongycastle.util.encoders.Hex
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Class used to represent a generic Graphene transaction.
 * [Transaction details](https://dev-doc.myecho.app/structgraphene_1_1chain_1_1transaction.html)
 *
 * @author Daria Pechkovskaya
 */
class Transaction
/**
 * Constructor used to build a Transaction object without a private key. This kind of object
 * is used to represent a transaction data that we don't intend to serialize and sign.
 *
 * @param blockData:  Block data instance, containing information about the location of this
 *                    transaction in the blockchain.
 * @param operations: The list of operations included in this transaction.
 */
    (var blockData: BlockData, var operations: List<BaseOperation>, var chainId: String) :
    ByteSerializable, JsonSerializable {

    var privateKeys = mutableListOf<ByteArray>()
    private val extensions: Extensions = Extensions()

    /**
     * This method is used to query whether the instance has a private keys.
     *
     * @return
     */
    val hasPrivateKeys: Boolean = privateKeys.isNotEmpty()

    /**
     * Adds required public key to list for signature purposes
     */
    fun addPrivateKey(key: ByteArray) {
        privateKeys.add(key)
    }

    /**
     * Updates the fees for all operations in this transaction.
     *
     * @param fees: New fees to apply
     */
    fun setFees(fees: List<AssetAmount>) {
        operations.forEachIndexed { i, operation ->
            operation.fee = fees[i]
        }
    }

    /**
     * Method that creates a serialized byte array with compact information about this transaction
     * that is needed for the creation of a signature.
     *
     * @return byte array with serialized information about this transaction.
     */
    override fun toBytes(): ByteArray {
        val chainBytes = Hex.decode(chainId)
        val blockDataBytes = blockData.toBytes()
        val operationsSizeBytes = operations.size.toByte()

        var operationBytes = byteArrayOf()
        operations.forEach { operation ->
            operationBytes += operation.id
            operationBytes += operation.toBytes()
        }

        val extensionsBytes = extensions.toBytes()

        return chainBytes + blockDataBytes + operationsSizeBytes + operationBytes + extensionsBytes
    }

    override fun toJsonString(): String? {
        return null
    }

    override fun toJsonObject(): JsonElement =
        JsonObject().apply {
            val expirationTimeMillis = TimeUnit.SECONDS.toMillis(blockData.relativeExpiration)
            val dateJson = Date(expirationTimeMillis).format()
            addProperty(KEY_EXPIRATION, dateJson)

            val operationsJson = JsonArray()
            operations.forEach { operation ->
                operationsJson.add(operation.toJsonObject())
            }
            add(KEY_OPERATIONS, operationsJson)

            add(KEY_EXTENSIONS, JsonArray())

            addProperty(KEY_REF_BLOCK_NUM, blockData.refBlockNum)
            addProperty(KEY_REF_BLOCK_PREFIX, blockData.refBlockPrefix)
        }

    /**
     * Json deserializer for [Transaction] class
     */
    class TransactionDeserializer : JsonDeserializer<Transaction> {

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): Transaction {
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

            return Transaction(blockData, operations, "")
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
        const val KEY_EXPIRATION = "expiration"
        const val KEY_SIGNATURES = "signatures"
        const val KEY_OPERATIONS = "operations"
        const val KEY_EXTENSIONS = Extensions.KEY_EXTENSIONS
        const val KEY_REF_BLOCK_NUM = "ref_block_num"
        const val KEY_REF_BLOCK_PREFIX = "ref_block_prefix"

        const val DEFAULT_EXPIRATION_TIME = 40
    }

}
