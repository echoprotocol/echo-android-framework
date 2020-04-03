package org.echo.mobile.framework.model.operations

import com.google.common.primitives.UnsignedLong
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.BaseOperation
import org.echo.mobile.framework.model.ERC20Token
import org.echo.mobile.framework.model.ERC20Withdrawal
import java.lang.reflect.Type

/**
 * Encapsulates erc20 token [erc20Token] burning information in ECHO network
 *
 * @author Dmitriy Bushuev
 */
class SidechainERC20BurnSocketOperation @JvmOverloads constructor(
    var account: Account,
    var amount: String,
    var erc20Withdrawal: ERC20Withdrawal,
    var erc20Token: ERC20Token,
    override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)
) : BaseOperation(OperationType.SIDECHAIN_ERC20_BURN_OPERATION) {

    override fun toBytes(): ByteArray = byteArrayOf()

    override fun toJsonString(): String {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(
            SidechainERC20BurnSocketOperation::class.java,
            SidechainERC20BurnSerializer()
        )
        return gsonBuilder.create().toJson(this)
    }

    override fun toJsonObject(): JsonElement = JsonArray().apply {
        add(id)
        val jsonObject = JsonObject().apply {
            add(KEY_FEE, fee.toJsonObject())
            addProperty(ACCOUNT_KEY, account.getObjectId())
            addProperty(AMOUNT_KEY, amount)
            addProperty(WITHDRAW_ID_KEY, erc20Withdrawal.getObjectId())
            addProperty(TOKEN_KEY, erc20Token.getObjectId())
            add(KEY_EXTENSIONS, extensions.toJsonObject())
        }
        add(jsonObject)
    }

    /**
     * Encapsulates logic of [SidechainERC20BurnSocketOperation] json serialization
     */
    class SidechainERC20BurnSerializer : JsonSerializer<SidechainERC20BurnSocketOperation> {

        override fun serialize(
            issueAsset: SidechainERC20BurnSocketOperation,
            type: Type,
            jsonSerializationContext: JsonSerializationContext
        ): JsonElement = JsonArray().apply {
            add(issueAsset.id)
            add(issueAsset.toJsonObject())
        }
    }

    /**
     * Deserializer for [SidechainERC20BurnSocketOperation]
     */
    class SidechainERC20BurnDeserializer : JsonDeserializer<SidechainERC20BurnSocketOperation> {

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): SidechainERC20BurnSocketOperation? {

            if (json == null || !json.isJsonObject) return null

            val jsonObject = json.asJsonObject

            val amount = jsonObject.get(AMOUNT_KEY).asString
            val fee = context.deserialize<AssetAmount>(
                jsonObject.get(KEY_FEE),
                AssetAmount::class.java
            ) ?: AssetAmount(UnsignedLong.ZERO)

            val account = Account(jsonObject.get(ACCOUNT_KEY).asString)
            val depositId = jsonObject.get(WITHDRAW_ID_KEY).asString
            val tokenId = jsonObject.get(TOKEN_KEY).asString

            return SidechainERC20BurnSocketOperation(
                account,
                amount,
                ERC20Withdrawal(depositId),
                ERC20Token(tokenId),
                fee
            )
        }
    }

    companion object {
        const val ACCOUNT_KEY = "account"
        const val AMOUNT_KEY = "amount"
        const val WITHDRAW_ID_KEY = "withdraw"
        const val TOKEN_KEY = "token"
        const val KEY_EXTENSIONS = "extensions"
    }
}
