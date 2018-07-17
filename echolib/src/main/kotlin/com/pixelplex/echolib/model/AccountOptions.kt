package com.pixelplex.echolib.model

import java.util.ArrayList

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
class AccountOptions {

    var memoKey: PublicKey? = null

    var votingAccount: Account = Account(Account.PROXY_TO_SELF)

    var witnessCount: Int = 0

    var committeeCount: Int = 0

    var votes: Array<Vote> = arrayOf()

    private val extensions = ArrayList<JsonSerializable>()

    constructor()

    constructor(memoKey: PublicKey) {
        this.memoKey = memoKey
    }

}
