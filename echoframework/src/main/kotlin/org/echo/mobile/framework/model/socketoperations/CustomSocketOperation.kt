package org.echo.mobile.framework.model.socketoperations

import com.google.gson.JsonElement
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.JsonDeserializable

/**
 * Includes classes for working with custom blockchain calls
 *
 * @author Daria Pechkovskaya
 */

/**
 * Represents custom call operation to blockchain API
 */
class CustomSocketOperation<T>(
    override val apiId: Int,
    callId: Int,
    private val operation: CustomOperation<T>,
    callback: Callback<T>

) : SocketOperation<T>(operation.method, callId, operation.type, callback) {

    override fun createParameters(): JsonElement =
        operation.createParameters()

    override fun fromJson(json: String): T? =
        operation.fromJson(json)
}

/**
 * Represents operation data for custom call to blockchain API
 */
abstract class CustomOperation<T>(
    val method: SocketMethodType,
    val type: Class<T>
) : JsonDeserializable<T> {

    /**
     * Creates json of call parameters
     * @return JsonObject representation
     */
    abstract fun createParameters(): JsonElement

}
