package com.pixelplex.echoframework.model.operations

import com.google.common.primitives.UnsignedLong
import com.google.gson.*
import com.pixelplex.echoframework.model.Account
import com.pixelplex.echoframework.model.AssetAmount
import com.pixelplex.echoframework.model.BaseOperation
import com.pixelplex.echoframework.model.Memo
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

    var memo = Memo()

    override fun toBytes(): ByteArray {
        val feeBytes = fee.toBytes()
        val issuerBytes = issuer.toBytes()
        val assetToIssueBytes = assetToIssue.toBytes()
        val issueToAccountBytes = issueToAccount.toBytes()
        val memoBytes = memo.toBytes()
        val extensions = extensions.toBytes()
        return feeBytes + issuerBytes + assetToIssueBytes + issueToAccountBytes + memoBytes + extensions
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
            if (memo.byteMessage != null)
                add(KEY_MEMO, memo.toJsonObject())
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

    companion object {
        const val ISSUER_KEY = "issuer"
        const val ASSET_TO_ISSUE_KEY = "asset_to_issue"
        const val ISSUE_TO_ACCOUNT_KEY = "issue_to_account"
        const val KEY_MEMO = "memo"
        const val KEY_EXTENSIONS = "extensions"
    }
}
