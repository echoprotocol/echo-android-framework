package org.echo.mobile.framework.model

import com.google.common.primitives.Bytes
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.exception.MalformedAddressException
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.support.Uint16
import java.lang.reflect.Type

/**
 * Contains user account additional information
 *
 * These are the fields which can be updated by the active authority
 *
 * [Account options details][https://dev-doc.myecho.app/structgraphene_1_1chain_1_1account__options.html]
 *
 * @author Dmitriy Bushuev
 */
class AccountOptions : GrapheneSerializable {

    var delegatingAccount: Account = Account(Account.PROXY_TO_SELF)

    var delegateShare: Int = 0

    private val extensions = Extensions()

    override fun toBytes(): ByteArray {

        // Adding delegating account
        val delegatingAccountBytes = delegatingAccount.toBytes()

        // Adding delegating account
        val delegateShare = Uint16.serialize(delegateShare)

        // Account options's extensions
        val extensionsBytes = extensions.toBytes()

        return Bytes.concat(
            delegatingAccountBytes,
            delegateShare,
            extensionsBytes
        )
    }

    override fun toJsonString(): String? = null

    override fun toJsonObject(): JsonElement? =
        JsonObject().apply {
            addProperty(KEY_DELEGATING_ACCOUNT, delegatingAccount.getObjectId())
            addProperty(KEY_DELEGATE_SHARE, delegateShare)
            add(KEY_EXTENSIONS, extensions.toJsonObject())
        }

    /**
     * Deserializer used to build a [AccountOptions] instance from the full JSON-formatted response
     * obtained by the API call.
     */
    class Deserializer(val network: Network) : JsonDeserializer<AccountOptions> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): AccountOptions? {

            if (json == null || !json.isJsonObject) {
                return null
            }

            val jsonAccountOptions = json.asJsonObject

            return try {
                AccountOptions()
            } catch (e: MalformedAddressException) {
                LOGGER.log("Invalid address deserialization", e)
                AccountOptions()
            }.apply {
                delegatingAccount = Account(jsonAccountOptions.get(KEY_DELEGATING_ACCOUNT).asString)
            }
        }
    }

    companion object {
        private val LOGGER = LoggerCoreComponent.create(AccountOptions::class.java.name)

        const val KEY_DELEGATING_ACCOUNT = "delegating_account"
        const val KEY_DELEGATE_SHARE = "delegate_share"
        const val KEY_EXTENSIONS = Extensions.KEY_EXTENSIONS
    }

}
