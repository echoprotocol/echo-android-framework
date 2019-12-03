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
import org.echo.mobile.framework.model.ERC20Token
import org.echo.mobile.framework.support.Uint8
import org.echo.mobile.framework.support.serialize
import org.spongycastle.util.encoders.Hex
import java.lang.reflect.Type

/**
 * Transfers [value] amount ethereum token [erc20Token] to ethereum address [ethAddress]
 *
 * @param account Required account for withdraw operation
 * @param ethAddress Target eth address
 * @param value Transfer amount
 *
 * @author Dmitriy Bushuev
 */
class WithdrawERC20Operation constructor(
    var account: Account,
    val ethAddress: String,
    val erc20Token: ERC20Token,
    val value: String,
    override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)
) : BaseOperation(OperationType.SIDECHAIN_ERC20_WITHDRAW_TOKEN_OPERATION) {

    override fun toBytes(): ByteArray {
        val feeBytes = fee.toBytes()
        val accountIdBytes = account.toBytes()

        val addressHex = Hex.decode(ethAddress)
        val addressBytes = byteArrayOf(addressHex.count().toByte()) + addressHex

        val erc20TokenBytes = erc20Token.toBytes()

        val valueBytes = Uint8.serialize(value.length) + value.toByteArray()
        val extensionBytes = extensions.toBytes()

        return Bytes.concat(
            feeBytes,
            accountIdBytes,
            addressBytes,
            erc20TokenBytes,
            valueBytes,
            extensionBytes
        )
    }

    override fun toJsonString(): String? = toJsonObject().toString()

    override fun toJsonObject(): JsonElement =
        JsonArray().apply {
            add(id)

            val accountUpdate = JsonObject().apply {
                add(KEY_FEE, fee.toJsonObject())
                addProperty(ACCOUNT_ID_KEY, account.toJsonString())
                addProperty(ETH_ADDRESS_KEY, ethAddress)
                addProperty(ETH_ADDRESS_KEY, ethAddress)
                addProperty(VALUE_KEY, value)
                addProperty(ERC_TOKEN_KEY, erc20Token.toJsonString())
                add(AccountUpdateOperation.KEY_EXTENSIONS, extensions.toJsonObject())
            }

            add(accountUpdate)
        }

    override fun toString(): String = "${javaClass.simpleName}(${toJsonObject()})"

    /**
     * JSON deserializer for [WithdrawERC20Operation]
     */
    class WithdrawErc20OperationDeserializer :
        JsonDeserializer<WithdrawERC20Operation> {

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): WithdrawERC20Operation? {
            if (json == null || !json.isJsonObject) return null

            val jsonObject = json.asJsonObject

            val fee = context.deserialize<AssetAmount>(
                jsonObject.get(KEY_FEE),
                AssetAmount::class.java
            )

            val account = Account(jsonObject.get(ACCOUNT_ID_KEY).asString)
            val ethAddress = jsonObject.get(ETH_ADDRESS_KEY).asString
            val erc20Token = ERC20Token(jsonObject.get(ETH_ADDRESS_KEY).asString)
            val value = jsonObject.getAsJsonPrimitive(VALUE_KEY).asString

            return WithdrawERC20Operation(account, ethAddress, erc20Token, value, fee)
        }
    }

    companion object {
        private const val ACCOUNT_ID_KEY = "account"
        private const val ETH_ADDRESS_KEY = "to"
        private const val ERC_TOKEN_KEY = "erc20_token"
        private const val VALUE_KEY = "value"
    }

}
