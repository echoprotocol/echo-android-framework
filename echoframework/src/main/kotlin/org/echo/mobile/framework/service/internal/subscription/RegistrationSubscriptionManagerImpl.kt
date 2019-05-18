package org.echo.mobile.framework.service.internal.subscription

import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.RegistrationResult
import org.echo.mobile.framework.service.RegistrationSubscriptionManager
import org.echo.mobile.framework.support.toJsonObject
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of [RegistrationSubscriptionManager]
 *
 * @author Daria Pechkovskaya
 */
class RegistrationSubscriptionManagerImpl : RegistrationSubscriptionManager {

    private val callbacks =
        ConcurrentHashMap<String, Callback<RegistrationResult>>()

    private val gson = configureGson()
    private val type = object : TypeToken<RegistrationResult>() {}.type

    override fun register(callId: String, callback: Callback<RegistrationResult>) {
        callbacks[callId] = callback
    }

    override fun clear() = callbacks.clear()

    override fun tryProcessEvent(event: String) {
        val (callId, dataParam) = getDataParam(event) ?: return
        val callback = callbacks.remove(callId)

        try {
            val jsonResult = dataParam?.asJsonArray?.firstOrNull()

            if (jsonResult?.isJsonObject == true) {
                val result = gson.fromJson<RegistrationResult>(jsonResult, type)

                callback?.onSuccess(result) ?: return
            }

        } catch (ex: Exception) {
            LOGGER.log("Error while parsing registration result.", ex)
            callback?.onError(LocalException("Error while parsing registration result.", ex))
                ?: return
        }
    }

    private fun getDataParam(event: String): Pair<String, JsonElement?>? {
        val params = event.toJsonObject()?.getAsJsonArray(PARAMS_KEY) ?: return null

        if (params.size() == 0) return null

        return params[0].toString() to params[1].asJsonArray
    }

    private fun configureGson() = GsonBuilder().create()

    companion object {
        private const val PARAMS_KEY = "params"
        private val LOGGER =
            LoggerCoreComponent.create(RegistrationSubscriptionManagerImpl::class.java.name)
    }


}
