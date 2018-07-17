package com.pixelplex.echolib.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.pixelplex.echolib.exception.MalformedAddressException
import java.lang.reflect.Type
import java.util.*

/**
 * Class used to represent the weighted set of keys and accounts that must approve operations.
 *
 * {@see [Authority](https://bitshares.org/doxygen/structgraphene_1_1chain_1_1authority.html)}
 *
 * @author Dmitriy Bushuev
 */
class Authority {

    var weightThreshold: Long = 1

    var keyAuthorities: HashMap<PublicKey, Long> = hashMapOf()

    var accountAuthorities: HashMap<Account, Long> = hashMapOf()

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

    @JvmOverloads
    constructor(
        weightThreshold: Long = 1,
        keyAuthorities: HashMap<PublicKey, Long> = hashMapOf(),
        accountAuthorities: HashMap<Account, Long> = hashMapOf()
    ) {
        this.weightThreshold = weightThreshold
        this.keyAuthorities = keyAuthorities
        this.accountAuthorities = accountAuthorities
    }

    companion object {
        const val KEY_WEIGHT_THRESHOLD = "weight_threshold"
        const val KEY_ACCOUNT_AUTHS = "account_auths"
        const val KEY_KEY_AUTHS = "key_auths"
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
    class Deserializer : JsonDeserializer<Authority> {

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Authority? {

            if (json == null || !json.isJsonObject) {
                return null
            }

            val baseObject = json.asJsonObject

            val weightThreshold = baseObject.get(KEY_WEIGHT_THRESHOLD).asLong
            val keyAuthArray = baseObject.getAsJsonArray(KEY_KEY_AUTHS)
            val accountAuthArray = baseObject.getAsJsonArray(KEY_ACCOUNT_AUTHS)

            val keyAuthMap = HashMap<PublicKey, Long>()
            for (i in 0 until keyAuthArray.size()) {
                val subArray = keyAuthArray.get(i).asJsonArray
                val addr = subArray.get(0).asString
                val weight = subArray.get(1).asLong
                try {
                    keyAuthMap[Address(addr).pubKey] = weight
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
