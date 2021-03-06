package org.echo.mobile.framework.model.socketoperations

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AccountOptions
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.AssetOptions
import org.echo.mobile.framework.model.Block
import org.echo.mobile.framework.model.Deposit
import org.echo.mobile.framework.model.DepositDeserializer
import org.echo.mobile.framework.model.operations.GenerateBitcoinAddressOperation
import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.model.Withdraw
import org.echo.mobile.framework.model.WithdrawDeserializer
import org.echo.mobile.framework.model.eddsa.EdAuthority
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.model.operations.AccountCreateOperation
import org.echo.mobile.framework.model.operations.AccountUpdateOperation
import org.echo.mobile.framework.model.operations.BlockRewardOperation
import org.echo.mobile.framework.model.operations.ContractCallOperation
import org.echo.mobile.framework.model.operations.ContractCreateOperation
import org.echo.mobile.framework.model.operations.CreateAssetOperation
import org.echo.mobile.framework.model.operations.GenerateEthereumAddressOperation
import org.echo.mobile.framework.model.operations.IssueAssetOperation
import org.echo.mobile.framework.model.operations.SidechainBurnSocketOperation
import org.echo.mobile.framework.model.operations.SidechainERC20BurnSocketOperation
import org.echo.mobile.framework.model.operations.SidechainERC20DepositSocketOperation
import org.echo.mobile.framework.model.operations.SidechainERC20IssueSocketOperation
import org.echo.mobile.framework.model.operations.SidechainERC20RegisterTokenOperation
import org.echo.mobile.framework.model.operations.SidechainIssueSocketOperation
import org.echo.mobile.framework.model.operations.TransferOperation
import org.echo.mobile.framework.model.operations.WithdrawBitcoinOperation
import org.echo.mobile.framework.model.operations.WithdrawERC20Operation
import org.echo.mobile.framework.model.operations.WithdrawEthereumOperation

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

        val result = jsonTree.asJsonObject.get(RESULT_KEY)
        if (!result.isJsonObject) return null

        return configureGson().fromJson<Block>(
            result.asJsonObject,
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
            BlockRewardOperation::class.java,
            BlockRewardOperation.BlockRewardDeserializer()
        )
        registerTypeAdapter(
            ContractCreateOperation::class.java,
            ContractCreateOperation.Deserializer()
        )
        registerTypeAdapter(
            ContractCallOperation::class.java,
            ContractCallOperation.Deserializer()
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
            IssueAssetOperation::class.java,
            IssueAssetOperation.IssueAssetDeserializer()
        )
        registerTypeAdapter(
            GenerateEthereumAddressOperation::class.java,
            GenerateEthereumAddressOperation.GenerateEthereumAddressDeserializer()
        )
        registerTypeAdapter(
            WithdrawBitcoinOperation::class.java,
            WithdrawBitcoinOperation.WithdrawBitcoinOperationDeserializer()
        )
        registerTypeAdapter(
            WithdrawERC20Operation::class.java,
            WithdrawERC20Operation.WithdrawErc20OperationDeserializer()
        )
        registerTypeAdapter(
            SidechainERC20RegisterTokenOperation::class.java,
            SidechainERC20RegisterTokenOperation.SidechainERC20RegisterTokenOperationDeserializer()
        )
        registerTypeAdapter(
            SidechainIssueSocketOperation::class.java,
            SidechainIssueSocketOperation.SidechainIssueDeserializer()
        )
        registerTypeAdapter(
            SidechainERC20BurnSocketOperation::class.java,
            SidechainERC20BurnSocketOperation.SidechainERC20BurnDeserializer()
        )
        registerTypeAdapter(
            SidechainBurnSocketOperation::class.java,
            SidechainBurnSocketOperation.SidechainBurnDeserializer()
        )
        registerTypeAdapter(
            SidechainERC20IssueSocketOperation::class.java,
            SidechainERC20IssueSocketOperation.SidechainERC20IssueDeserializer()
        )
        registerTypeAdapter(
            SidechainERC20DepositSocketOperation::class.java,
            SidechainERC20DepositSocketOperation.SidechainERC20DepositDeserializer()
        )
        registerTypeAdapter(
            SidechainERC20DepositSocketOperation::class.java,
            SidechainERC20DepositSocketOperation.SidechainERC20DepositDeserializer()
        )
        registerTypeAdapter(
            GenerateBitcoinAddressOperation::class.java,
            GenerateBitcoinAddressOperation.GenerateBitcoinAddressDeserializer()
        )
        registerTypeAdapter(
            WithdrawEthereumOperation::class.java,
            WithdrawEthereumOperation.WithdrawEthereumOperationDeserializer()
        )
        registerTypeAdapter(AssetAmount::class.java, AssetAmount.Deserializer())
        registerTypeAdapter(EdAuthority::class.java, EdAuthority.Deserializer())
        registerTypeAdapter(Account::class.java, Account.Deserializer())
        registerTypeAdapter(AccountOptions::class.java, AccountOptions.Deserializer(network))
        registerTypeAdapter(Withdraw::class.java, WithdrawDeserializer())
        registerTypeAdapter(Deposit::class.java, DepositDeserializer())
    }.create()


}
