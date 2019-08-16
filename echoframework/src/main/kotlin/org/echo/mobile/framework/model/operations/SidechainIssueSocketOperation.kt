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
import org.echo.mobile.framework.model.EthDeposit
import java.lang.reflect.Type

/**
 * Encapsulates ethereum asset issue information
 *
 * @author Dmitriy Bushuev
 */
class SidechainIssueSocketOperation @JvmOverloads constructor(
    var account: Account,
    var value: AssetAmount,
    var deposit: EthDeposit,
    override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)
) : BaseOperation(OperationType.SIDECHAIN_ETH_ISSUE_OPERATION) {

    override fun toBytes(): ByteArray = byteArrayOf()

    override fun toJsonString(): String {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(
            SidechainIssueSocketOperation::class.java,
            SidechainIssueSocketOperation.SidechainIssueSerializer()
        )
        return gsonBuilder.create().toJson(this)
    }

    override fun toJsonObject(): JsonElement = JsonArray().apply {
        add(id)
        val jsonObject = JsonObject().apply {
            add(KEY_FEE, fee.toJsonObject())
            addProperty(ACCOUNT_KEY, account.getObjectId())
            add(ASSET_KEY, value.toJsonObject())
            addProperty(DEPOSIT_ID_KEY, deposit.getObjectId())
            add(KEY_EXTENSIONS, extensions.toJsonObject())
        }
        add(jsonObject)
    }

    /**
     * Encapsulates logic of [SidechainIssueSocketOperation] json serialization
     */
    class SidechainIssueSerializer : JsonSerializer<SidechainIssueSocketOperation> {

        override fun serialize(
            issueAsset: SidechainIssueSocketOperation,
            type: Type,
            jsonSerializationContext: JsonSerializationContext
        ): JsonElement = JsonArray().apply {
            add(issueAsset.id)
            add(issueAsset.toJsonObject())
        }
    }

    /**
     * Deserializer for [SidechainIssueSocketOperation]
     */
    class SidechainIssueDeserializer : JsonDeserializer<SidechainIssueSocketOperation> {

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): SidechainIssueSocketOperation? {

            if (json == null || !json.isJsonObject) return null

            val jsonObject = json.asJsonObject

            val amount = context.deserialize<AssetAmount>(
                jsonObject.get(ASSET_KEY),
                AssetAmount::class.java
            )
            val fee = context.deserialize<AssetAmount>(
                jsonObject.get(KEY_FEE),
                AssetAmount::class.java
            )

            val account = Account(jsonObject.get(ACCOUNT_KEY).asString)
            val depositId = jsonObject.get(DEPOSIT_ID_KEY).asString

            return SidechainIssueSocketOperation(
                account,
                amount,
                EthDeposit(depositId),
                fee
            )
        }
    }

    companion object {
        const val ACCOUNT_KEY = "account"
        const val ASSET_KEY = "value"
        const val DEPOSIT_ID_KEY = "deposit_id"
        const val KEY_EXTENSIONS = "extensions"
    }
}
