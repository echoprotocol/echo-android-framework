package com.pixelplex.echolib.facade.internal

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.core.crypto.CryptoCoreComponent
import com.pixelplex.echolib.exception.LocalException
import com.pixelplex.echolib.facade.AuthenticationFacade
import com.pixelplex.echolib.model.Account
import com.pixelplex.echolib.model.FullUserAccount
import com.pixelplex.echolib.service.AccountHistoryApiService
import com.pixelplex.echolib.service.DatabaseApiService

/**
 * Implementation of [AuthenticationFacade]
 *
 * <p>
 *     Delegates API call logic to [AccountHistoryApiService]
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class AuthenticationFacadeImpl(
    private val accountHistoryApiService: AccountHistoryApiService,
    private val databaseApiService: DatabaseApiService,
    private val cryptoCoreComponent: CryptoCoreComponent
) : AuthenticationFacade {

    override fun login(name: String, password: String, callback: Callback<Account>) {

        databaseApiService.getFullAccounts(
            listOf(name),
            false,
            object : Callback<List<FullUserAccount>> {
                override fun onSuccess(result: List<FullUserAccount>) {
                    val address = cryptoCoreComponent.getAddress(name, password)
                }

                override fun onError(error: LocalException) {
                    callback.onError(error)
                }
            })

    }

    override fun changePassword(
        nameOrId: String,
        oldPassword: String,
        newPassword: String,
        callback: Callback<Account>
    ) {
    }

}
