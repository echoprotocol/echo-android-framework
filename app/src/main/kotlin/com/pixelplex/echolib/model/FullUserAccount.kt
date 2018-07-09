package com.pixelplex.echolib.model

import com.google.gson.annotations.Expose

/**
 * Represents full information about user account
 *(@see
 * https://bitshares.org/doxygen/structgraphene_1_1app_1_1full__account.html#a41c05d75dd96571f6d189f409df3f590)
 *
 * @author Dmitriy Bushuev
 */
class FullUserAccount {

    @Expose
    lateinit var balances: List<Balance>

    lateinit var userAccountId: String

    lateinit var userKey: ByteArray

    lateinit var userName: String

    lateinit var account: Account

}
