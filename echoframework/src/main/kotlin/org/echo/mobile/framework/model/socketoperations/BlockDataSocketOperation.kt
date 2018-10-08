package org.echo.mobile.framework.model.socketoperations

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.ILLEGAL_ID
import org.echo.mobile.framework.model.DynamicGlobalProperties

/**
 * Returns the block chain’s rapidly-changing properties. The returned object contains information
 * that changes every block interval such as the head block number, the next witness, etc.
 *
 * @return [DynamicGlobalProperties]
 *
 * @author Daria Pechkovskaya
 */
class BlockDataSocketOperation(
    override val apiId: Int,
    callId: Int,
    callback: Callback<DynamicGlobalProperties>
) : SocketOperation<DynamicGlobalProperties>(
    SocketMethodType.CALL,
    callId,
    DynamicGlobalProperties::class.java,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.BLOCK_DATA.key)
            add(JsonArray())
        }

    override fun fromJson(json: String): DynamicGlobalProperties? {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        try {
            val result = jsonTree.asJsonObject.get(RESULT_KEY)?.asJsonObject

            return configureGson().fromJson<DynamicGlobalProperties>(
                result,
                DynamicGlobalProperties::class.java
            )

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return null
    }

    private fun configureGson() = GsonBuilder().apply {
        registerTypeAdapter(
            DynamicGlobalProperties::class.java,
            DynamicGlobalProperties.Deserializer()
        )
    }.create()


}
