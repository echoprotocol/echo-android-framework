package org.echo.mobile.framework.service.internal.subscription

import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AccountOptions
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.AssetOptions
import org.echo.mobile.framework.model.TransactionOperationsResult
import org.echo.mobile.framework.model.TransactionResult
import org.echo.mobile.framework.model.eddsa.EdAuthority
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.model.operations.AccountCreateOperation
import org.echo.mobile.framework.model.operations.AccountUpdateOperation
import org.echo.mobile.framework.model.operations.ContractCallOperation
import org.echo.mobile.framework.model.operations.ContractCreateOperation
import org.echo.mobile.framework.model.operations.ContractTransferOperation
import org.echo.mobile.framework.model.operations.CreateAssetOperation
import org.echo.mobile.framework.model.operations.GenerateEthereumAddressOperation
import org.echo.mobile.framework.model.operations.IssueAssetOperation
import org.echo.mobile.framework.model.operations.TransferOperation
import org.echo.mobile.framework.model.operations.WithdrawEthereumOperation
import org.echo.mobile.framework.service.TransactionSubscriptionManager
import org.echo.mobile.framework.support.toJsonObject
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of [TransactionSubscriptionManager]
 *
 * @author Daria Pechkovskaya
 */
class TransactionSubscriptionManagerImpl(network: Network) : TransactionSubscriptionManager {

    private val callbacks =
        ConcurrentHashMap<String, Callback<TransactionResult>>()

    private val gson = configureGson(network)
    private val type = object : TypeToken<TransactionResult>() {}.type

    override fun register(callId: String, callback: Callback<TransactionResult>) {
        callbacks[callId] = callback
    }

    override fun clear() = callbacks.clear()

    override fun tryProcessEvent(event: String) {
        val (callId, dataParam) = getDataParam(event) ?: return
        val callback = callbacks.remove(callId)

        try {
            val jsonResult = dataParam?.asJsonArray?.firstOrNull()

            if (jsonResult?.isJsonObject == true) {
                val result = gson.fromJson<TransactionResult>(jsonResult, type)

                callback?.onSuccess(result) ?: return
            }

        } catch (ex: Exception) {
            LOGGER.log("Error while parsing transaction result.", ex)
            callback?.onError(LocalException("Error while parsing transaction result.", ex))
                ?: return
        }
    }

    private fun getDataParam(event: String): Pair<String, JsonElement?>? {
        val params = event.toJsonObject()?.getAsJsonArray(PARAMS_KEY) ?: return null

        if (params.size() == 0) return null

        return params[0].toString() to params[1].asJsonArray
    }

    private fun configureGson(network: Network) = GsonBuilder().apply {
        registerTypeAdapter(
            TransactionOperationsResult::class.java,
            TransactionOperationsResult.Deserializer()
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
            ContractCreateOperation::class.java,
            ContractCreateOperation.Deserializer()
        )
        registerTypeAdapter(
            ContractCallOperation::class.java,
            ContractCallOperation.Deserializer()
        )
        registerTypeAdapter(
            ContractTransferOperation::class.java,
            ContractTransferOperation.Deserializer()
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
            WithdrawEthereumOperation::class.java,
            WithdrawEthereumOperation.WithdrawEthereumOperationDeserializer()
        )
        registerTypeAdapter(AssetAmount::class.java, AssetAmount.Deserializer())
        registerTypeAdapter(EdAuthority::class.java, EdAuthority.Deserializer())
        registerTypeAdapter(Account::class.java, Account.Deserializer())
        registerTypeAdapter(AccountOptions::class.java, AccountOptions.Deserializer(network))
    }.create()

    companion object {
        private const val PARAMS_KEY = "params"
        private val LOGGER =
            LoggerCoreComponent.create(TransactionSubscriptionManagerImpl::class.java.name)
    }

}
