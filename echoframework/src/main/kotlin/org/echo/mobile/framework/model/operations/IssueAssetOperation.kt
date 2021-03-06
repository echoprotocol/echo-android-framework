package org.echo.mobile.framework.model.operations

import com.google.common.primitives.Bytes
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
import java.lang.reflect.Type

/**
 * Encapsulates asset issue operation information
 *
 * @author Dmitriy Bushuev
 */
class IssueAssetOperation @JvmOverloads constructor(
    var issuer: Account,
    var assetToIssue: AssetAmount,
    var issueToAccount: Account,
    override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)
) : BaseOperation(OperationType.ASSET_ISSUE_OPERATION) {

    override fun toBytes(): ByteArray {
        val feeBytes = fee.toBytes()
        val issuerBytes = issuer.toBytes()
        val assetToIssueBytes = assetToIssue.toBytes()
        val issueToAccountBytes = issueToAccount.toBytes()
        val extensions = extensions.toBytes()
        return Bytes.concat(
            feeBytes,
            issuerBytes,
            assetToIssueBytes,
            issueToAccountBytes,
            extensions
        )
    }

    override fun toJsonString(): String {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(
            IssueAssetOperation::class.java,
            IssueAssetSerializer()
        )
        return gsonBuilder.create().toJson(this)
    }

    override fun toJsonObject(): JsonElement = JsonArray().apply {
        add(id)
        val jsonObject = JsonObject().apply {
            add(KEY_FEE, fee.toJsonObject())
            addProperty(ISSUER_KEY, issuer.getObjectId())
            add(ASSET_TO_ISSUE_KEY, assetToIssue.toJsonObject())
            addProperty(ISSUE_TO_ACCOUNT_KEY, issueToAccount.getObjectId())
            add(KEY_EXTENSIONS, extensions.toJsonObject())
        }
        add(jsonObject)
    }

    /**
     * Encapsulates logic of transfer json serialization
     */
    class IssueAssetSerializer : JsonSerializer<IssueAssetOperation> {

        override fun serialize(
            issueAsset: IssueAssetOperation,
            type: Type,
            jsonSerializationContext: JsonSerializationContext
        ): JsonElement = JsonArray().apply {
            add(issueAsset.id)
            add(issueAsset.toJsonObject())
        }
    }

    /**
     * This deserializer will work on any transfer operation serialized in the 'array form' used a lot in
     * the Graphene Blockchain API.
     *
     * An example of this serialized form is the following:
     *
     * {
     *      "fee": {
     *          "amount": 264174,
     *          "asset_id": "1.3.0"
     *      },
     *      "from": "1.2.138632",
     *      "to": "1.2.129848",
     *      "amount": {
     *          "amount": 100,
     *          "asset_id": "1.3.0"
     *      },
     *      "extensions": []
     * }
     *
     * It will convert this data into a nice TransferOperation object.
     */
    class IssueAssetDeserializer : JsonDeserializer<IssueAssetOperation> {

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): IssueAssetOperation? {

            if (json == null || !json.isJsonObject) return null

            val jsonObject = json.asJsonObject

            val amount = context.deserialize<AssetAmount>(
                jsonObject.get(ASSET_TO_ISSUE_KEY),
                AssetAmount::class.java
            )
            val fee = context.deserialize<AssetAmount>(
                jsonObject.get(KEY_FEE),
                AssetAmount::class.java
            )

            val issuer = Account(jsonObject.get(ISSUER_KEY).asString)
            val issueTarget = Account(jsonObject.get(ISSUE_TO_ACCOUNT_KEY).asString)

            return IssueAssetOperation(
                issuer,
                amount,
                issueTarget,
                fee
            )
        }
    }

    companion object {
        const val ISSUER_KEY = "issuer"
        const val ASSET_TO_ISSUE_KEY = "asset_to_issue"
        const val ISSUE_TO_ACCOUNT_KEY = "issue_to_account"
        const val KEY_MEMO = "memo"
        const val KEY_EXTENSIONS = "extensions"
    }
}
