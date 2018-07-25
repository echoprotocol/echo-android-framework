package com.pixelplex.echoframework.model.socketoperations

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.ILLEGAL_ID
import com.pixelplex.echoframework.model.*
import com.pixelplex.echoframework.model.network.Network
import com.pixelplex.echoframework.model.operations.AccountUpdateOperation

/**
 * Retrieve a full, signed block.
 *
 * @param blockNumber Height of the block whose header should be returned
 * @return the referenced [Block] object, or null if no matching block was found
 *
 * @author Daria Pechkovskaya
 * @author Dmitriy Bushuev
 */
class GetBlockSocketOperation(
    override val apiId: Int,
    val blockNumber: String,
    method: SocketMethodType = SocketMethodType.CALL,
    callback: Callback<Block>,
    private val network: Network

) : SocketOperation<Block>(method, ILLEGAL_ID, Block::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.BLOCK.key)
            add(JsonArray().apply { add(blockNumber) })
        }

    override fun fromJson(json: String): Block? {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        val result = jsonTree.asJsonObject.get(RESULT_KEY)?.asJsonObject ?: return null

        val gson = configureGson()

        return gson.fromJson<Block>(
            result,
            Block::class.java
        )
    }

    private fun configureGson() = GsonBuilder().apply {
        registerTypeAdapter(
            Block::class.java,
            Block.BlockDeserializer()
        )
        registerTypeAdapter(
            Transaction::class.java,
            Transaction.TransactionDeserializer()
        )
        registerTypeAdapter(
            AccountUpdateOperation::class.java,
            AccountUpdateOperation.Deserializer()
        )
        registerTypeAdapter(
            TransferOperation::class.java,
            TransferOperation.TransferDeserializer()
        )
        registerTypeAdapter(AssetAmount::class.java, AssetAmount.Deserializer())
        registerTypeAdapter(Authority::class.java, Authority.Deserializer(network))
        registerTypeAdapter(Account::class.java, Account.Deserializer())
        registerTypeAdapter(AccountOptions::class.java, AccountOptions.Deserializer(network))
    }.create()

    companion object {
        private const val RESULT_KEY = "result"
    }

}
