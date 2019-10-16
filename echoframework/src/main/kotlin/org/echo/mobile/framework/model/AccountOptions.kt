package org.echo.mobile.framework.model

import com.google.common.primitives.Bytes
import com.google.gson.*
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.exception.MalformedAddressException
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.support.Uint16
import org.echo.mobile.framework.support.serialize
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

    var votingAccount: Account = Account(Account.PROXY_TO_SELF)

    var delegatingAccount: Account = Account(Account.PROXY_TO_SELF)

    var delegateShare: Int = 0

    var committeeCount: Int = 0

    var votes: Array<Vote> = arrayOf()

    private val extensions = Extensions()

    override fun toBytes(): ByteArray {

        // Adding voting account
        val votingAccountBytes = votingAccount.toBytes()

        // Adding delegating account
        val delegatingAccountBytes = delegatingAccount.toBytes()

        // Adding delegating account
        val delegateShare = Uint16.serialize(delegateShare)

        // Adding num_committee
        val committeeCountBytes = Uint16.serialize(committeeCount)

        // Vote's array length
        val votesBytes = votes.serialize { vote -> vote.toBytes() }

        // Account options's extensions
        val extensionsBytes = extensions.toBytes()

        return Bytes.concat(
            votingAccountBytes,
            delegatingAccountBytes,
            delegateShare,
            committeeCountBytes,
            votesBytes,
            extensionsBytes
        )
    }

    override fun toJsonString(): String? = null

    override fun toJsonObject(): JsonElement? =
        JsonObject().apply {
            addProperty(KEY_NUM_COMMITTEE, committeeCount)
            addProperty(KEY_VOTING_ACCOUNT, votingAccount.getObjectId())
            addProperty(KEY_DELEGATING_ACCOUNT, delegatingAccount.getObjectId())
            addProperty(KEY_DELEGATE_SHARE, delegateShare)

            val votesArray = JsonArray().apply {
                votes.forEach { vote -> add(vote.toString()) }
            }

            add(KEY_VOTES, votesArray)
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
                votingAccount = Account(jsonAccountOptions.get(KEY_VOTING_ACCOUNT).asString)
                delegatingAccount = Account(jsonAccountOptions.get(KEY_DELEGATING_ACCOUNT).asString)
                committeeCount = jsonAccountOptions.get(KEY_NUM_COMMITTEE).asInt
            }
        }
    }

    companion object {
        private val LOGGER = LoggerCoreComponent.create(AccountOptions::class.java.name)

        const val KEY_NUM_COMMITTEE = "num_committee"
        const val KEY_VOTES = "votes"
        const val KEY_VOTING_ACCOUNT = "voting_account"
        const val KEY_DELEGATING_ACCOUNT = "delegating_account"
        const val KEY_DELEGATE_SHARE = "delegate_share"
        const val KEY_EXTENSIONS = Extensions.KEY_EXTENSIONS
    }

}
