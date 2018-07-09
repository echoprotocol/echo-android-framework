package com.pixelplex.echolib.facade.internal

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.facade.AuthenticationFacade
import com.pixelplex.echolib.model.Account
import com.pixelplex.echolib.service.AccountHistoryApiService

/**
 * Implementation of [AuthenticationFacade]
 *
 * <p>
 *     Delegates API call logic to [AccountHistoryApiService]
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class AuthenticationFacadeImpl(private val accountHistoryApiService: AccountHistoryApiService) :
    AuthenticationFacade {

    override fun login(name: String, password: String, callback: Callback<Account>) {
    }

    override fun changePassword(
        nameOrId: String,
        oldPassword: String,
        newPassword: String,
        callback: Callback<Account>
    ) {
    }

}
