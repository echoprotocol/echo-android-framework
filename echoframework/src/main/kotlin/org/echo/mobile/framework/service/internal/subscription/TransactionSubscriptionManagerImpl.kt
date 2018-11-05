package org.echo.mobile.framework.service.internal.subscription

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.*
import org.echo.mobile.framework.model.contract.output.ContractAddressOutputValueType
import org.echo.mobile.framework.model.contract.output.ContractOutputDecoder
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.model.operations.*
import org.echo.mobile.framework.service.ContractSubscriptionManager
import org.echo.mobile.framework.service.TransactionSubscriptionManager
import org.echo.mobile.framework.service.UpdateListener
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
            val jsonResult = dataParam?.asJsonArray?.firstOrNull()?.asJsonObject
            val result = gson.fromJson<TransactionResult>(jsonResult, type)

            callback?.onSuccess(result) ?: return

        } catch (ex: JsonParseException) {
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
            ContractOperation::class.java,
            ContractOperation.Deserializer()
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

    companion object {
        private const val PARAMS_KEY = "params"
        private val LOGGER =
            LoggerCoreComponent.create(TransactionSubscriptionManagerImpl::class.java.name)
    }


}
