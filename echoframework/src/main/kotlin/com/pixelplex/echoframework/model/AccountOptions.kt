package com.pixelplex.echoframework.model

import com.google.gson.*
import com.pixelplex.bitcoinj.revert
import com.pixelplex.echoframework.exception.MalformedAddressException
import com.pixelplex.echoframework.model.network.Network
import java.lang.reflect.Type

/**
 * Contains user account additional information
 *
 * <p>
 *     These are the fields which can be updated by the active authority
 * </p>
 *
 * (@see https://bitshares.org/doxygen/structgraphene_1_1chain_1_1account__options.html#details)
 *
 * @author Dmitriy Bushuev
 */
class AccountOptions : GrapheneSerializable {

    var memoKey: PublicKey? = null

    var votingAccount: Account = Account(Account.PROXY_TO_SELF)

    var witnessCount: Int = 0

    var committeeCount: Int = 0

    var votes: Array<String> = arrayOf()

    private val extensions = Extensions()

    constructor()

    constructor(memoKey: PublicKey) {
        this.memoKey = memoKey
    }

    override fun toBytes(): ByteArray =
        memoKey?.let { memo ->
            // Adding byte to indicate that there is memo data
            var bytes = byteArrayOf(1.toByte())

            // Adding memo key
            bytes += memo.toBytes()

            // Adding voting account
            bytes += votingAccount.toBytes()

            // Adding num_witness
            bytes += witnessCount.toShort().revert()

            // Adding num_committee
            bytes += committeeCount.toShort().revert()

            // Vote's array length
            bytes += votes.size.toByte()
            votes.forEach { vote ->
                bytes += vote.toByteArray()
            }

            // Account options's extensions
            bytes += extensions.toBytes()

            bytes
        } ?: let {
            byteArrayOf(0.toByte())
        }


    override fun toJsonString(): String? = null

    override fun toJsonObject(): JsonElement? =
       JsonObject().apply {
           addProperty(KEY_MEMO_KEY, Address(memoKey!!).toString())
           addProperty(KEY_NUM_COMMITTEE, committeeCount)
           addProperty(KEY_NUM_WITNESS, witnessCount)
           addProperty(KEY_VOTING_ACCOUNT, votingAccount.getObjectId())
           val votesArray = JsonArray()
           add(KEY_VOTES, votesArray)
           add(KEY_EXTENSIONS, extensions.toJsonObject())
       }

    companion object {
        const val KEY_MEMO_KEY = "memo_key"
        const val KEY_NUM_COMMITTEE = "num_committee"
        const val KEY_NUM_WITNESS = "num_witness"
        const val KEY_VOTES = "votes"
        const val KEY_VOTING_ACCOUNT = "voting_account"
        const val KEY_EXTENSIONS = Extensions.KEY_EXTENSIONS
    }

    /**
     * Deserializer used to build a [AccountOptions] instance from the full JSON-formatted response
     * obtained by the API call.
     */
    class Deserializer(val network: Network): JsonDeserializer<AccountOptions>{
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): AccountOptions? {

            if(json == null || !json.isJsonObject){
                return null
            }

            val jsonAccountOptions = json.asJsonObject

            val options: AccountOptions = try {
                val memoKeyString = jsonAccountOptions.get(KEY_MEMO_KEY).asString
                val address = Address(memoKeyString, network)
                AccountOptions(address.pubKey)
            } catch (e: MalformedAddressException) {
                System.out.println("MalformedAddressException. Msg: " + e.message)
                AccountOptions()
            }

            options.votingAccount = Account(jsonAccountOptions.get(KEY_VOTING_ACCOUNT).asString)
            options.witnessCount = jsonAccountOptions.get(KEY_NUM_WITNESS).asInt
            options.committeeCount = jsonAccountOptions.get(KEY_NUM_COMMITTEE).asInt

            return options
        }
    }

}
