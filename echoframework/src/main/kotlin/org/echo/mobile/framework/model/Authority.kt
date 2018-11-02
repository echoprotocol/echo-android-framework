package org.echo.mobile.framework.model

import com.google.common.primitives.Bytes
import com.google.gson.*
import org.echo.mobile.framework.support.Uint16
import org.echo.mobile.framework.support.Uint32
import org.echo.mobile.framework.support.serialize
import org.echo.mobile.framework.exception.MalformedAddressException
import org.echo.mobile.framework.model.network.Network
import java.lang.reflect.Type

/**
 * Class used to represent the weighted set of keys and accounts that must approve operations.
 *
 * [Authority details](https://dev-doc.myecho.app/structgraphene_1_1chain_1_1authority.html)
 *
 * @author Dmitriy Bushuev
 */
class Authority @JvmOverloads constructor(
    var weightThreshold: Int = 1,
    var keyAuthorities: Map<PublicKey, Long> = mapOf(),
    var accountAuthorities: Map<Account, Long> = mapOf()
) : GrapheneSerializable {

    private val extensions: Extensions = Extensions()

    /**
     * @return: Returns a list of public keys linked to this authority
     */
    val keyAuthList: List<PublicKey>
        get() = keyAuthorities.keys.toList()

    /**
     * @return: Returns a list of accounts linked to this authority
     */
    val accountAuthList: List<Account>
        get() = accountAuthorities.keys.toList()

    override fun toBytes(): ByteArray {
        val authsSize = accountAuthorities.size + keyAuthorities.size
        val authsSizeBytes = byteArrayOf(authsSize.toByte())

        // If the authority is not empty of references, we serialize its contents
        // otherwise its only contribution will be a zero byte
        if (authsSize > 0) {
            // Weight threshold
            val weightThresholdBytes = Uint32.serialize(weightThreshold)

            // Serializing individual accounts and their corresponding weights
            val accountAuthoritiesBytes =
                accountAuthorities.serialize(
                    keyToBytes = { account -> account.toBytes() },
                    valueToBytes = { value -> Uint16.serialize(value) })

            // Serializing individual keys and their corresponding weights
            val keyAuthoritiesBytes =
                keyAuthorities.serialize(
                    keyToBytes = { account -> account.toBytes() },
                    valueToBytes = { value -> Uint16.serialize(value) })

            // Adding number of extensions
            val extensionsBytes = extensions.toBytes()

            return Bytes.concat(
                authsSizeBytes,
                weightThresholdBytes,
                accountAuthoritiesBytes,
                keyAuthoritiesBytes,
                extensionsBytes
            )
        }

        return authsSizeBytes
    }

    override fun toJsonString(): String? = null

    override fun toJsonObject(): JsonElement =
        JsonObject().apply {
            addProperty(KEY_WEIGHT_THRESHOLD, weightThreshold)

            val keyAuthArray = JsonArray().apply {
                keyAuthorities.forEach { (publicKey, weight) ->
                    val subArray = JsonArray()
                    val address = Address(publicKey)
                    subArray.add(address.toString())
                    subArray.add(weight)
                    add(subArray)
                }
            }

            val accountAuthArray = JsonArray().apply {
                accountAuthorities.forEach { (account, weight) ->
                    val subArray = JsonArray()
                    subArray.add(account.toString())
                    subArray.add(weight) //keyAuthorities?
                }
            }

            add(KEY_KEY_AUTHS, keyAuthArray)
            add(KEY_ACCOUNT_AUTHS, accountAuthArray)
            add(KEY_EXTENSIONS, extensions.toJsonObject())
        }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Authority) {
            return false
        }

        return other.keyAuthorities == this.keyAuthorities &&
                other.accountAuthorities == this.accountAuthorities &&
                other.weightThreshold == this.weightThreshold
    }

    override fun hashCode(): Int {
        return keyAuthorities.hashCode() +
                accountAuthorities.hashCode() * 31 +
                weightThreshold.hashCode() * 31
    }

    companion object {
        const val KEY_WEIGHT_THRESHOLD = "weight_threshold"
        const val KEY_ACCOUNT_AUTHS = "account_auths"
        const val KEY_KEY_AUTHS = "key_auths"
        const val KEY_EXTENSIONS = Extensions.KEY_EXTENSIONS
    }

    /**
     * Custom deserializer used while parsing the 'get_account' API call response.
     *
     * This will deserialize an account authority in the form:
     *
     * {
     *   "weight_threshold": 1,
     *   "account_auths": [],
     *   "key_auths": [["BTS6yoiaoC4p23n31AV4GnMy5QDh5yUQEUmU4PmNxRQPGg7jjPkBq",1]],
     *   "address_auths": []
     * }
     */
    class Deserializer(val network: Network) : JsonDeserializer<Authority> {

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Authority? {

            if (json == null || !json.isJsonObject) {
                return null
            }

            val baseObject = json.asJsonObject

            val weightThreshold = baseObject.get(KEY_WEIGHT_THRESHOLD).asInt
            val keyAuthArray = baseObject.getAsJsonArray(KEY_KEY_AUTHS)
            val accountAuthArray = baseObject.getAsJsonArray(KEY_ACCOUNT_AUTHS)

            val keyAuthMap = HashMap<PublicKey, Long>()
            for (i in 0 until keyAuthArray.size()) {
                val subArray = keyAuthArray.get(i).asJsonArray
                val addr = subArray.get(0).asString
                val weight = subArray.get(1).asLong
                try {
                    keyAuthMap[Address(addr, network).pubKey] = weight
                } catch (e: MalformedAddressException) {
                    System.out.println("MalformedAddressException. Msg: " + e.message)
                }

            }

            val accountAuthMap = HashMap<Account, Long>()
            for (i in 0 until accountAuthArray.size()) {
                val subArray = accountAuthArray.get(i).asJsonArray
                val userId = subArray.get(0).asString
                val weight = subArray.get(1).asLong
                val userAccount = Account(userId)
                accountAuthMap[userAccount] = weight
            }

            return Authority(weightThreshold, keyAuthMap, accountAuthMap)
        }
    }

}
