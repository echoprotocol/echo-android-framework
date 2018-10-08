package org.echo.mobile.framework.service.internal

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import org.echo.mobile.framework.core.mapper.ObjectMapper
import org.echo.mobile.framework.model.DynamicGlobalProperties
import org.echo.mobile.framework.service.CurrentBlockchainDataSubscriptionManager
import org.echo.mobile.framework.service.UpdateListener
import org.echo.mobile.framework.support.toJsonObject

/**
 * Implementation of [CurrentBlockchainDataSubscriptionManager]
 *
 * @author Daria Pechkovskaya
 */
class CurrentBlockchainDataSubscriptionManagerImpl : CurrentBlockchainDataSubscriptionManager {

    private val listeners = mutableListOf<UpdateListener<DynamicGlobalProperties>>()

    override val mapper: ObjectMapper<DynamicGlobalProperties> = DynamicGlobalPropertiesMapper()

    override fun addListener(listener: UpdateListener<DynamicGlobalProperties>) {
        listeners.add(listener)
    }

    override fun containListeners(): Boolean = listeners.isNotEmpty()

    override fun containsListener(listener: UpdateListener<DynamicGlobalProperties>): Boolean =
        listeners.contains(listener)

    override fun removeListener(listener: UpdateListener<DynamicGlobalProperties>) {
        listeners.remove(listener)
    }

    override fun clear() = listeners.clear()

    override fun notify(blockchainData: DynamicGlobalProperties) {
        listeners.forEach { listener ->
            listener.onUpdate(blockchainData)
        }
    }

    override fun processEvent(event: String): DynamicGlobalProperties? {
        try {
            val dataParam = getDataParam(event)?.asJsonArray

            val dynamicGlobalPropertiesJson = dataParam?.firstOrNull()?.asJsonArray?.firstOrNull()
                ?: return null

            return mapper.map(dynamicGlobalPropertiesJson.toString())
        } catch (ex: JsonParseException) {
            return null
        }
    }

    private fun getDataParam(event: String): JsonElement? {
        val params = event.toJsonObject()?.getAsJsonArray(PARAMS_KEY) ?: return null

        if (params.size() == 0) {
            return null
        }

        return params[1].asJsonArray
    }

    private class DynamicGlobalPropertiesMapper : ObjectMapper<DynamicGlobalProperties> {
        private val gson = configureGson()
        override fun map(data: String): DynamicGlobalProperties? =
            gson.fromJson<DynamicGlobalProperties>(data, DynamicGlobalProperties::class.java)

        private fun configureGson() = GsonBuilder().apply {
            registerTypeAdapter(
                DynamicGlobalProperties::class.java,
                DynamicGlobalProperties.Deserializer()
            )
        }.create()
    }

    companion object {
        private const val PARAMS_KEY = "params"
    }
}
