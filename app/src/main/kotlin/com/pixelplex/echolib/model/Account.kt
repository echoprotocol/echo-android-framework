package com.pixelplex.echolib.model

import com.google.gson.annotations.Expose

/**
 * Represents account model in Graphene blockchain
 * (@see https://bitshares.org/doxygen/classgraphene_1_1chain_1_1account__object.html)
 *
 * @author Dmitriy Bushuev
 */
class Account : GrapheneObject {

    @Expose
    lateinit var name: String

    @Expose
    lateinit var owner: Authority

    @Expose
    lateinit var active: Authority

    @Expose
    lateinit var options: AccountOptions

    @Expose
    lateinit var statistics: String

    @Expose
    var membershipExpirationDate: Long = 0

    @Expose
    lateinit var registrar: String

    @Expose
    lateinit var referrer: String

    @Expose
    lateinit var lifetimeReferrer: String

    @Expose
    var networkFeePercentage: Long = 0

    @Expose
    var lifetimeReferrerFeePercentage: Long = 0

    @Expose
    var referrerRewardsPercentage: Long = 0

    /**
     * Requires a user account in the string representation, that is in the 1.2.x format.
     */
    constructor(id: String) : super(id)

    /**
     * Constructor that requires the proper graphene object id and an account name,
     * that represent user account
     */
    constructor(id: String, name: String) : super(id) {
        this.name = name
    }

    companion object {
        const val PROXY_TO_SELF = "1.2.5"
    }

}
