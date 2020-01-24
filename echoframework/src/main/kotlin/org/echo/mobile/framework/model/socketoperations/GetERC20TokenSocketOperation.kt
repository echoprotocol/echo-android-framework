package org.echo.mobile.framework.model.socketoperations

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.ERC20Token
import org.echo.mobile.framework.model.Erc20TokenDeserializer

/**
 * Retrieves information about erc20 token by [contactAddress]
 *
 * @author Dmitriy Bushuev
 */
class GetERC20TokenSocketOperation(
    override val apiId: Int,
    private val contactAddress: String,
    callId: Int,
    callback: Callback<ERC20Token>
) : SocketOperation<ERC20Token>(
    SocketMethodType.CALL,
    callId,
    ERC20Token::class.java,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.GET_ERC20_TOKEN.key)

            val addressJson = JsonArray().apply {
                add(contactAddress)
            }

            add(addressJson)
        }

    override fun fromJson(json: String): ERC20Token? {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        try {
            val result = jsonTree.asJsonObject.get(RESULT_KEY)

            return GsonBuilder().registerTypeAdapter(ERC20Token::class.java, Erc20TokenDeserializer())
                .create()
                .fromJson<ERC20Token>(result, ERC20Token::class.java)

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return null
    }
}
