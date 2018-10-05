package org.echo.mobile.framework.model.socketoperations

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.*
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.model.operations.AccountCreateOperation
import org.echo.mobile.framework.model.operations.AccountUpdateOperation
import org.echo.mobile.framework.model.operations.ContractOperation
import org.echo.mobile.framework.model.operations.CreateAssetOperation
import org.echo.mobile.framework.model.operations.IssueAssetOperation
import org.echo.mobile.framework.model.operations.TransferOperation

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
    callId: Int,
    callback: Callback<Block>,
    private val network: Network

) : SocketOperation<Block>(SocketMethodType.CALL, callId, Block::class.java, callback) {

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

        return configureGson().fromJson<Block>(
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
            AccountCreateOperation::class.java,
            AccountCreateOperation.Deserializer()
        )
        registerTypeAdapter(
            TransferOperation::class.java,
            TransferOperation.TransferDeserializer()
        )
        registerTypeAdapter(
            ContractOperation::class.java,
            ContractOperation.Deserializer()
        )

        registerTypeAdapter(
            CreateAssetOperation::class.java,
            CreateAssetOperation.CreateAssetDeserializer()
        )
        registerTypeAdapter(
            AssetOptions::class.java,
            AssetOptions.AssetOptionsDeserializer()
        )
        registerTypeAdapter(
            Memo::class.java,
            Memo.MemoDeserializer(network)
        )
        registerTypeAdapter(
            IssueAssetOperation::class.java,
            IssueAssetOperation.IssueAssetDeserializer()
        )
        registerTypeAdapter(AssetAmount::class.java, AssetAmount.Deserializer())
        registerTypeAdapter(Authority::class.java, Authority.Deserializer(network))
        registerTypeAdapter(Account::class.java, Account.Deserializer())
        registerTypeAdapter(AccountOptions::class.java, AccountOptions.Deserializer(network))
    }.create()


}