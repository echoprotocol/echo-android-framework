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
import org.echo.mobile.framework.support.Int64
import org.spongycastle.util.encoders.Hex
import java.lang.reflect.Type

/**
 * Transfers ethereum asset from echo blockchain to ethereum address
 *
 * @param account Required account for withdraw operation
 * @param ethAddress Target eth address
 * @param value Transfer amount
 *
 * @author Dmitriy Bushuev
 */
class WithdrawEthereumOperation constructor(
    var account: Account,
    val ethAddress: String,
    val value: UnsignedLong,
    override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)
) : BaseOperation(OperationType.SIDECHAIN_ETH_WITHDRAW_OPERATION) {

    override fun toBytes(): ByteArray {
        val feeBytes = fee.toBytes()
        val accountIdBytes = account.toBytes()

        val addressHex = Hex.decode(ethAddress)
        val addressBytes = byteArrayOf(addressHex.count().toByte()) + addressHex

        val valueBytes = Int64.serialize(value)
        val extensionBytes = extensions.toBytes()

        return Bytes.concat(feeBytes, accountIdBytes, addressBytes, valueBytes, extensionBytes)
    }

    override fun toJsonString(): String? = toJsonObject().toString()

    override fun toJsonObject(): JsonElement =
        JsonArray().apply {
            add(id)

            val accountUpdate = JsonObject().apply {
                add(KEY_FEE, fee.toJsonObject())
                addProperty(ACCOUNT_ID_KEY, account.toJsonString())
                addProperty(ETH_ADDRESS_KEY, ethAddress)
                addProperty(VALUE_KEY, value)
                add(AccountUpdateOperation.KEY_EXTENSIONS, extensions.toJsonObject())
            }

            add(accountUpdate)
        }

    override fun toString(): String = "${javaClass.simpleName}(${toJsonObject()})"

    /**
     * JSON deserializer for [WithdrawEthereumOperation]
     */
    class WithdrawEthereumOperationDeserializer :
        JsonDeserializer<WithdrawEthereumOperation> {

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): WithdrawEthereumOperation? {
            if (json == null || !json.isJsonObject) return null

            val jsonObject = json.asJsonObject

            val fee = context.deserialize<AssetAmount>(
                jsonObject.get(KEY_FEE),
                AssetAmount::class.java
            )

            val account = Account(jsonObject.get(ACCOUNT_ID_KEY).asString)
            val ethAddress = jsonObject.get(ETH_ADDRESS_KEY).asString
            val value = UnsignedLong.valueOf(jsonObject.getAsJsonPrimitive(VALUE_KEY).asString)

            return WithdrawEthereumOperation(account, ethAddress, value, fee)
        }
    }

    companion object {
        private const val ACCOUNT_ID_KEY = "account"
        private const val ETH_ADDRESS_KEY = "eth_addr"
        private const val VALUE_KEY = "value"
    }

}
