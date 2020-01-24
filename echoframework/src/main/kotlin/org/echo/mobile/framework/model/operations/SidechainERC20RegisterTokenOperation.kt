package org.echo.mobile.framework.model.operations

import com.google.common.primitives.Bytes
import com.google.common.primitives.UnsignedLong
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.BaseOperation
import org.echo.mobile.framework.support.Uint8
import org.echo.mobile.framework.support.serialize
import org.spongycastle.util.encoders.Hex
import java.lang.reflect.Type

/**
 * Registers ethereum ERC20 token by [ethAddress] in ECHO network
 *
 * @author Dmitriy Bushuev
 */
class SidechainERC20RegisterTokenOperation constructor(
    var account: Account,
    val ethAddress: String,
    val name: String,
    val symbol: String,
    val decimals: UnsignedLong,
    override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)
) : BaseOperation(OperationType.SIDECHAIN_ERC20_REGISTER_TOKEN_OPERATION) {

    override fun toBytes(): ByteArray {
        val feeBytes = fee.toBytes()
        val accountIdBytes = account.toBytes()

        val addressHex = Hex.decode(ethAddress)
        val addressBytes = byteArrayOf(addressHex.count().toByte()) + addressHex

        val nameBytes = Uint8.serialize(name.length) + name.toByteArray()
        val symbolBytes = Uint8.serialize(symbol.length) + symbol.toByteArray()
        val decimalsBytes = Uint8.serialize(decimals)
        val extensionBytes = extensions.toBytes()

        return Bytes.concat(
            feeBytes,
            accountIdBytes,
            addressBytes,
            nameBytes,
            symbolBytes,
            decimalsBytes,
            extensionBytes
        )
    }

    override fun toJsonString(): String? = toJsonObject().toString()

    override fun toJsonObject(): JsonElement =
        JsonArray().apply {
            add(id)

            val accountUpdate = JsonObject().apply {
                add(KEY_FEE, fee.toJsonObject())
                addProperty(ACCOUNT_KEY, account.toJsonString())
                addProperty(ETH_ADDRESS_KEY, ethAddress)
                addProperty(NAME_KEY, name)
                addProperty(SYMBOL_KEY, symbol)
                addProperty(DECIMALS_KEY, decimals)
                add(AccountUpdateOperation.KEY_EXTENSIONS, extensions.toJsonObject())
            }

            add(accountUpdate)
        }

    override fun toString(): String = "${javaClass.simpleName}(${toJsonObject()})"

    /**
     * JSON deserializer for [SidechainERC20RegisterTokenOperation]
     */
    class SidechainERC20RegisterTokenOperationDeserializer :
        JsonDeserializer<SidechainERC20RegisterTokenOperation> {

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): SidechainERC20RegisterTokenOperation? {
            if (json == null || !json.isJsonObject) return null

            val jsonObject = json.asJsonObject

            val fee = context.deserialize<AssetAmount>(
                jsonObject.get(KEY_FEE),
                AssetAmount::class.java
            )

            val account = Account(jsonObject.get(ACCOUNT_KEY).asString)
            val ethAddress = jsonObject.get(ETH_ADDRESS_KEY).asString
            val name = jsonObject.getAsJsonPrimitive(NAME_KEY).asString
            val symbol = jsonObject.getAsJsonPrimitive(SYMBOL_KEY).asString
            val decimals =
                UnsignedLong.valueOf(jsonObject.getAsJsonPrimitive(DECIMALS_KEY).asString)

            return SidechainERC20RegisterTokenOperation(
                account,
                ethAddress,
                name,
                symbol,
                decimals,
                fee
            )
        }
    }

    companion object {
        private const val ACCOUNT_KEY = "account"
        private const val ETH_ADDRESS_KEY = "eth_addr"
        private const val NAME_KEY = "name"
        private const val SYMBOL_KEY = "symbol"
        private const val DECIMALS_KEY = "decimals"
    }

}
