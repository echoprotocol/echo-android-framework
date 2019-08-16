package org.echo.mobile.framework.model

import com.google.common.reflect.TypeToken
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

/**
 *  Represents block model in blockchain
 *
 * @author Daria Pechkovskaya
 * @author Dmitriy Bushuev
 */
class Block(
    var previous: String,
    var round: String,
    var timestamp: String,
    var account: Account,
    var delagate: Account,
    var transactionMerkleRoot: String,
    var rand: String,
    var edSignature: String,
    var transactions: List<Transaction>,
    var vmRoot: List<String>,
    var prevSignatures: List<PrevSignature>
) {

    /**
     * Json deserializer for [Block] class
     */
    class BlockDeserializer : JsonDeserializer<Block> {

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): Block {
            val jsonObject = json.asJsonObject

            // Parsing block data information
            val previous = jsonObject.get(KEY_PREVIOUS).asString
            val timestamp = jsonObject.get(KEY_TIMESTAMP).asString
            val round = jsonObject.get(KEY_ROUND).asString
            val rand = jsonObject.get(KEY_RAND).asString
            val transactionMerkleRoot = jsonObject.get(KEY_TRANSACTION_MERKLE_ROOT).asString
            val edSignature = jsonObject.get(KEY_ED_SIGNATURE).asString
            val account = Account(jsonObject.get(KEY_ACCOUNT).asString)
            val delegate = Account(jsonObject.get(KEY_DELEGATE).asString)

            // Parsing operation list
            val transactions = mutableListOf<Transaction>()

            val jsonTransactions = jsonObject.get(KEY_TRANSACTIONS).asJsonArray

            for (i in 0 until jsonTransactions.size()) {
                val operationBody = jsonTransactions[i]

                transactions.add(
                    context.deserialize(
                        operationBody,
                        Transaction::class.java
                    )
                )
            }

            val vmRoot = context.deserialize<List<String>>(
                jsonObject.get(KEY_VM_ROOT),
                object : TypeToken<List<String>>() {}.type
            )
            val prevSignatures = context.deserialize<List<PrevSignature>>(
                jsonObject.get(KEY_PREV_SIGNATURES),
                object : TypeToken<List<PrevSignature>>() {}.type
            )

            return Block(
                previous,
                round,
                timestamp,
                account,
                delegate,
                transactionMerkleRoot,
                rand,
                edSignature,
                transactions,
                vmRoot,
                prevSignatures
            )
        }
    }

    companion object {
        private const val KEY_PREVIOUS = "previous"
        private const val KEY_TIMESTAMP = "timestamp"
        private const val KEY_ACCOUNT = "account"
        private const val KEY_DELEGATE = "account"
        private const val KEY_TRANSACTION_MERKLE_ROOT = "transaction_merkle_root"
        private const val KEY_TRANSACTIONS = "transactions"
        private const val KEY_VM_ROOT = "vm_root"
        private const val KEY_PREV_SIGNATURES = "prev_signatures"
        private const val KEY_ROUND = "round"
        private const val KEY_RAND = "rand"
        private const val KEY_ED_SIGNATURE = "ed_signature"
    }

}

/**
 * Describes previous signature model
 */
class PrevSignature(
    @SerializedName("_step") val step: Long,
    @SerializedName("_leader") val leader: Long,
    @SerializedName("_signer") val signer: Long,
    @SerializedName("_delegate") val delegate: Long,
    @SerializedName("_fallback") val fallback: Long,
    @SerializedName("_bba_sign") val sign: String
)
