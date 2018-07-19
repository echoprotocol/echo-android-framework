package com.pixelplex.echolib.model.operations

import com.google.common.primitives.UnsignedLong
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.pixelplex.echolib.model.*

/**
 * Represents blockchain operation for updating an existing account.
 *
 * @author Daria Pechkovskaya
 */
class AccountUpdateOperation @JvmOverloads constructor(
    private val account: Account,
    owner: Authority?,
    active: Authority?,
    newOptions: AccountOptions?,
    override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)
) : BaseOperation(OperationType.ACCOUNT_UPDATE_OPERATION) {

    private var ownerOption = Optional(owner)
    private var activeOption = Optional(active)
    private var newOptionsOption = Optional(newOptions)

    /**
     * Updates owner value
     * @param owner New owner value
     */
    fun setOwner(owner: Authority) {
        this.ownerOption = Optional(owner)
    }

    /**
     * Updates active value
     * @param active New active value
     */
    fun setActive(active: Authority) {
        this.activeOption = Optional(active)
    }

    /**
     * Updates options value
     * @param options New options value
     */
    fun setAccountOptions(options: AccountOptions) {
        this.newOptionsOption = Optional(options)
    }

    override fun toBytes(): ByteArray {
        val feeBytes = fee.toBytes()
        val accountBytes = account.toBytes()
        val ownerBytes = ownerOption.toBytes()
        val activeBytes = activeOption.toBytes()
        val newOptionsBytes = newOptionsOption.toBytes()
        val extensionBytes = extensions.toBytes()
        return feeBytes + accountBytes + ownerBytes + activeBytes + newOptionsBytes + extensionBytes
    }

    override fun toJsonString(): String? = Gson().toJson(this)

    override fun toJsonObject(): JsonElement =
        JsonArray().apply {
            add(id)

            val accountUpdate = JsonObject().apply {
                add(KEY_FEE, fee.toJsonObject())
                addProperty(KEY_ACCOUNT, account.toJsonString())
                if (ownerOption.isSet)
                    add(KEY_OWNER, ownerOption.toJsonObject())
                if (activeOption.isSet)
                    add(KEY_ACTIVE, activeOption.toJsonObject())
                if (newOptionsOption.isSet)
                    add(KEY_NEW_OPTIONS, newOptionsOption.toJsonObject())
                add(KEY_EXTENSIONS, extensions.toJsonObject())
            }

            add(accountUpdate)
        }

    companion object {
        const val KEY_ACCOUNT = "account"
        const val KEY_OWNER = "owner"
        const val KEY_ACTIVE = "active"
        const val KEY_FEE = "fee"
        const val KEY_NEW_OPTIONS = "new_options"
        const val KEY_EXTENSIONS = "extensions"
    }
}
