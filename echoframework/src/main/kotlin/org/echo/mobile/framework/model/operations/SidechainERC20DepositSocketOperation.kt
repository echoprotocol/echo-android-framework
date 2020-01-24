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
import com.google.gson.reflect.TypeToken
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.BaseOperation
import java.lang.reflect.Type

/**
 * Encapsulates erc20 token [erc20TokenAddress] deposit information
 *
 * @author Dmitriy Bushuev
 */
class SidechainERC20DepositSocketOperation @JvmOverloads constructor(
    var committeeMember: Account,
    var maliciousCommitteemen: List<String>,
    var account: Account,
    var value: String,
    var erc20TokenAddress: String,
    var transactionHash: String,
    override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)
) : BaseOperation(OperationType.SIDECHAIN_ERC20_DEPOSIT_TOKEN_OPERATION) {

    override fun toBytes(): ByteArray = byteArrayOf()

    override fun toJsonString(): String {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(
            SidechainERC20DepositSocketOperation::class.java,
            SidechainERC20DepositSerializer()
        )
        return gsonBuilder.create().toJson(this)
    }

    override fun toJsonObject(): JsonElement = JsonArray().apply {
        add(id)
        val jsonObject = JsonObject().apply {
            add(KEY_FEE, fee.toJsonObject())
            add(COMMETTEE_MEMBER_KEY, committeeMember.toJsonObject())
            add(COMMITTEEMEN_KEY, JsonArray().apply { maliciousCommitteemen.forEach { add(it) } })
            addProperty(ACCOUNT_KEY, account.getObjectId())
            addProperty(VALUE_KEY, value)
            addProperty(TOKEN_ADDRESS_KEY, erc20TokenAddress)
            addProperty(TRANSACTION_HASH_KEY, transactionHash)
            add(KEY_EXTENSIONS, extensions.toJsonObject())
        }
        add(jsonObject)
    }

    /**
     * Encapsulates logic of [SidechainERC20DepositSocketOperation] json serialization
     */
    class SidechainERC20DepositSerializer : JsonSerializer<SidechainERC20DepositSocketOperation> {

        override fun serialize(
            issueAsset: SidechainERC20DepositSocketOperation,
            type: Type,
            jsonSerializationContext: JsonSerializationContext
        ): JsonElement = JsonArray().apply {
            add(issueAsset.id)
            add(issueAsset.toJsonObject())
        }
    }

    /**
     * Deserializer for [SidechainERC20DepositSocketOperation]
     */
    class SidechainERC20DepositDeserializer :
        JsonDeserializer<SidechainERC20DepositSocketOperation> {

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): SidechainERC20DepositSocketOperation? {

            if (json == null || !json.isJsonObject) return null

            val jsonObject = json.asJsonObject

            val value = jsonObject.get(VALUE_KEY).asString

            val fee = context.deserialize<AssetAmount>(
                jsonObject.get(KEY_FEE),
                AssetAmount::class.java
            ) ?: AssetAmount(UnsignedLong.ZERO)

            val account = Account(jsonObject.get(ACCOUNT_KEY).asString)
            val commetteeMember = Account(jsonObject.get(COMMETTEE_MEMBER_KEY).asString)

            val committeemen = context.deserialize<List<String>>(
                jsonObject.get(COMMITTEEMEN_KEY),
                object : TypeToken<List<String>>() {}.type
            )

            val transactionHash = jsonObject.get(TRANSACTION_HASH_KEY).asString
            val tokenAddress = jsonObject.get(TOKEN_ADDRESS_KEY).asString

            return SidechainERC20DepositSocketOperation(
                commetteeMember,
                committeemen,
                account,
                value,
                tokenAddress,
                transactionHash,
                fee
            )
        }
    }

    companion object {
        const val ACCOUNT_KEY = "account"
        const val VALUE_KEY = "value"
        const val COMMETTEE_MEMBER_KEY = "committee_member_id"
        const val COMMITTEEMEN_KEY = "malicious_committeemen"
        const val TOKEN_ADDRESS_KEY = "erc20_token_addr"
        const val TRANSACTION_HASH_KEY = "transaction_hash"
        const val KEY_EXTENSIONS = "extensions"
    }
}
