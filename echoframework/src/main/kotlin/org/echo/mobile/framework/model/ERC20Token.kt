package org.echo.mobile.framework.model

import com.google.common.primitives.UnsignedLong
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import org.echo.mobile.framework.core.mapper.ObjectMapper
import java.lang.reflect.Type

/**
 * Descibes model of ERC20 token in ECHO network
 */
class ERC20Token(
    id: String,
    var owner: Account? = null,
    val address: String = "",
    val contract: String = "",
    val name: String = "",
    val symbol: String = "",
    val decimals: UnsignedLong = UnsignedLong.valueOf(0)
) : GrapheneObject(id), GrapheneSerializable {

    override fun toJsonString(): String? = getObjectId()

    override fun toJsonObject(): JsonElement? = null

}

/**
 * Json mapper for [ERC20Token] model
 */
class Erc20TokenMapper : ObjectMapper<ERC20Token> {

    override fun map(data: String): ERC20Token? =
        try {
            GsonBuilder()
                .registerTypeAdapter(ERC20Token::class.java, Erc20TokenDeserializer())
                .create()
                .fromJson(data, ERC20Token::class.java)
        } catch (exception: Exception) {
            null
        }

}

/**
 * Json deserializer for [ERC20Token]
 */
class Erc20TokenDeserializer : JsonDeserializer<ERC20Token> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ERC20Token? {
        if (json == null || !json.isJsonObject) return null

        val jsonToken = json.asJsonObject

        val id = jsonToken.get(KEY_ID).asString
        val account = Account(jsonToken.get(KEY_OWNER).asString)
        val address = jsonToken.get(KEY_ADDRESS).asString
        val contract = jsonToken.get(KEY_CONTRACT).asString
        val name = jsonToken.get(KEY_NAME).asString
        val symbol = jsonToken.get(KEY_SYMBOL).asString
        val decimals = UnsignedLong.valueOf(jsonToken.get(KEY_DECIMALS).asString)

        return ERC20Token(id, account, address, contract, name, symbol, decimals)
    }

    companion object {
        private const val KEY_ID = "id"
        private const val KEY_OWNER = "owner"
        private const val KEY_ADDRESS = "eth_addr"
        private const val KEY_CONTRACT = "contract"
        private const val KEY_NAME = "name"
        private const val KEY_SYMBOL = "symbol"
        private const val KEY_DECIMALS = "decimals"
    }

}