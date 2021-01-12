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
 * Encapsulates claim balance operation information
 *
 * @author Pavel Savchkov
 */
class BalanceClaimOperation @JvmOverloads constructor(
        var depositToAccount: Account,
        var balanceToClaimId: String,
        var balanceOwnerKey: String,
        var totalClaimed: AssetAmount,
        override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)
) : BaseOperation(OperationType.BALANCE_CLAIM_OPERATION) {

    override fun toBytes(): ByteArray {
        val feeBytes = fee.toBytes()
        val depositToAccountBytes = depositToAccount.toBytes()
        val balanceToClaimIdBytes = balanceToClaimId.toByteArray()
        val balanceOwnerKeyBytes = balanceOwnerKey.toByteArray()
        val totalClaimedBytes = totalClaimed.toBytes()
        val extensions = extensions.toBytes()
        return Bytes.concat(
                feeBytes,
                depositToAccountBytes,
                balanceToClaimIdBytes,
                balanceOwnerKeyBytes,
                totalClaimedBytes,
                extensions
        )
    }

    override fun toJsonString(): String {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(
                BalanceClaimOperation::class.java,
                BalanceClaimSerializer()
        )
        return gsonBuilder.create().toJson(this)
    }

    override fun toJsonObject(): JsonElement = JsonArray().apply {
        add(id)
        val jsonObject = JsonObject().apply {
            add(KEY_FEE, fee.toJsonObject())
            addProperty(DEPOSIT_TO_ACCOUNT_KEY, depositToAccount.toJsonString())
            addProperty(BALANCE_TO_CLAIM_ID_KEY, balanceToClaimId)
            addProperty(BALANCE_OWNER_KEY, balanceOwnerKey)
            add(TOTAL_CLAIMED_KEY, totalClaimed.toJsonObject())
            add(EXTENSIONS_KEY, extensions.toJsonObject())
        }
        add(jsonObject)
    }

    /**
     * Encapsulates logic of json serialization
     */
    class BalanceClaimSerializer : JsonSerializer<BalanceClaimOperation> {

        override fun serialize(
                balanceClaimOperation: BalanceClaimOperation,
                type: Type,
                jsonSerializationContext: JsonSerializationContext
        ): JsonElement = JsonArray().apply {
            add(balanceClaimOperation.id)
            add(balanceClaimOperation.toJsonObject())
        }
    }

    /**
     * This deserializer will work on balance claim operation serialized
     * in the 'array form' used a lot in the Graphene Blockchain API.
     */
    class BalanceClaimDeserializer : JsonDeserializer<BalanceClaimOperation> {

        @Throws(JsonParseException::class)
        override fun deserialize(
                json: JsonElement?,
                typeOfT: Type,
                context: JsonDeserializationContext
        ): BalanceClaimOperation? {

            if (json == null || !json.isJsonObject) return null

            val jsonObject = json.asJsonObject

            val fee = context.deserialize<AssetAmount>(
                    jsonObject.get(KEY_FEE),
                    AssetAmount::class.java
            )

            val depositToAccount = Account(jsonObject.get(DEPOSIT_TO_ACCOUNT_KEY).asString)
            val balanceToClaimId = jsonObject.get(BALANCE_TO_CLAIM_ID_KEY).asString
            val balanceOwnerKey = jsonObject.get(BALANCE_OWNER_KEY).asString
            val totalClaimed =
                    context.deserialize<AssetAmount>(jsonObject.get(TOTAL_CLAIMED_KEY),
                            AssetAmount::class.java)

            return BalanceClaimOperation(
                    depositToAccount, balanceToClaimId, balanceOwnerKey, totalClaimed, fee
            )
        }
    }

    companion object {
        const val DEPOSIT_TO_ACCOUNT_KEY = "deposit_to_account"
        const val BALANCE_TO_CLAIM_ID_KEY = "balance_to_claim"
        const val BALANCE_OWNER_KEY = "balance_owner_key"
        const val TOTAL_CLAIMED_KEY = "total_claimed"
        const val EXTENSIONS_KEY = "extensions"
    }
}
