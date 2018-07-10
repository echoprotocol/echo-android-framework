package com.pixelplex.echolib.model

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.pixelplex.echolib.support.Signature
import com.pixelplex.echolib.support.format
import org.bitcoinj.core.ECKey
import org.spongycastle.util.encoders.Hex
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Class used to represent a generic Graphene transaction.
 * <a href="https://bitshares.org/doxygen/structgraphene_1_1chain_1_1transaction.html">Source</a>
 *
 * @author Daria Pechkovskaya
 */
class Transaction : ByteSerializable, JsonSerializable {

    var privateKey: ECKey? = null
        private set
    var blockData: BlockData
    var operations: List<BaseOperation>
        private set
    private val extensions: Extensions = Extensions()
    var chain: Chains = Chains.BITSHARES

    /**
     * Constructor used to build a Transaction object without a private key. This kind of object
     * is used to represent a transaction data that we don't intend to serialize and sign.
     * @param blockData: Block data instance, containing information about the location of this
     * transaction in the blockchain.
     * @param operations: The list of operations included in this transaction.
     */
    constructor(blockData: BlockData, operations: List<BaseOperation>) {
        this.blockData = blockData
        this.operations = operations
    }

    /**
     * Transaction constructor.
     * @param privateKey : Instance of a ECKey containing the private key that will be used to sign
     * this transaction.
     * @param blockData : Block data containing important information used to sign a transaction.
     * @param operations : List of operations to include in the transaction.
     */
    constructor(privateKey: ECKey, blockData: BlockData, operations: List<BaseOperation>) :
            this(blockData, operations) {
        this.privateKey = privateKey
    }

    /**
     * This method is used to query whether the instance has a private key.
     * @return
     */
    val hasPrivateKey: Boolean = privateKey != null

    /**
     * Updates the fees for all operations in this transaction.
     * @param fees: New fees to apply
     */
    fun setFees(fees: List<AssetAmount>) {
        operations.forEachIndexed { i, operation ->
            operation.setFee(fees[i])
        }
    }


    /**
     * Method that creates a serialized byte array with compact information about this transaction
     * that is needed for the creation of a signature.
     *
     * @return byte array with serialized information about this transaction.
     */
    override fun toBytes(): ByteArray {
        val chainBytes = Hex.decode(chain.id)
        val blockDataBytes = blockData.toBytes()
        val operationsSizeBytes = operations.size.toByte()

        var operationBytes = byteArrayOf()
        operations.forEach { operation ->
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

            val signature = Signature.signTransaction(this@Transaction)
            val signaturesJson = JsonArray().apply { add(Hex.toHexString(signature)) }
            add(KEY_SIGNATURES, signaturesJson)

            val operationsJson = JsonArray()
            operations.forEach { operation ->
                operationsJson.add(operation.toJsonObject())
            }
            add(KEY_OPERATIONS, operationsJson)

            add(KEY_EXTENSIONS, JsonArray())

            addProperty(KEY_REF_BLOCK_NUM, blockData.refBlockNum)
            addProperty(KEY_REF_BLOCK_PREFIX, blockData.refBlockPrefix)
        }

    companion object {
        const val KEY_EXPIRATION = "expiration"
        const val KEY_SIGNATURES = "signatures"
        const val KEY_OPERATIONS = "operations"
        const val KEY_EXTENSIONS = "extensions"
        const val KEY_REF_BLOCK_NUM = "ref_block_num"
        const val KEY_REF_BLOCK_PREFIX = "ref_block_prefix"
    }
}
