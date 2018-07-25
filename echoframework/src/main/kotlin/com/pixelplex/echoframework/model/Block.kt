package com.pixelplex.echoframework.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

/**
 *  Represents block model in Graphene blockchain
 * (@see https://bitshares.org/doxygen/structgraphene_1_1chain_1_1signed__block.html)
 *
 * @author Daria Pechkovskaya
 * @author Dmitriy Bushuev
 */
class Block(
    var previous: String,
    var timestamp: String,
    var witness: String,
    var transactionMerkleRoot: String,
    var witnessSignature: String,
    var transactions: List<Transaction>
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
            val witness = jsonObject.get(KEY_WITNESS).asString
            val transactionMerkleRoot = jsonObject.get(KEY_TRANSACTION_MERKLE_ROOT).asString
            val witnessSignature = jsonObject.get(KEY_WITNESS_SIGNATURE).asString

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

            return Block(
                previous,
                timestamp,
                witness,
                transactionMerkleRoot,
                witnessSignature,
                transactions
            )
        }
    }

    companion object {
        private const val KEY_PREVIOUS = "previous"
        private const val KEY_TIMESTAMP = "timestamp"
        private const val KEY_WITNESS = "witness"
        private const val KEY_TRANSACTION_MERKLE_ROOT = "transaction_merkle_root"
        private const val KEY_WITNESS_SIGNATURE = "witness_signature"
        private const val KEY_TRANSACTIONS = "transactions"
    }

}
