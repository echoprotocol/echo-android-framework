package com.pixelplex.echolib.model

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

}
