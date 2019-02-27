package org.echo.mobile.framework.model.socketoperations

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.FeeParameters
import org.echo.mobile.framework.model.GlobalProperties

/**
 * Get blockchain current global properties object
 *
 * @author Daria Pechkovskaya
 */
class GetGlobalPropertiesSocketOperation(
    override val apiId: Int,
    callId: Int,
    callback: Callback<GlobalProperties>
) : SocketOperation<GlobalProperties>(
    SocketMethodType.CALL,
    callId,
    GlobalProperties::class.java,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.GET_GLOBAL_PROPERTIES.key)
            add(JsonArray())
        }

    override fun fromJson(json: String): GlobalProperties? {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        val result = jsonTree.asJsonObject.get(RESULT_KEY)

        val gson = GsonBuilder()
            .registerTypeAdapter(FeeParameters::class.java, FeeParameters.Deserializer())
            .create()

        return gson.fromJson<GlobalProperties>(
            result,
            object : TypeToken<GlobalProperties>() {}.type
        )
    }
}
