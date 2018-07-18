package com.pixelplex.echolib.facade.internal

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.core.crypto.CryptoCoreComponent
import com.pixelplex.echolib.exception.LocalException
import com.pixelplex.echolib.exception.NotFoundException
import com.pixelplex.echolib.facade.AuthenticationFacade
import com.pixelplex.echolib.model.Account
import com.pixelplex.echolib.model.AuthorityType
import com.pixelplex.echolib.service.DatabaseApiService
import com.pixelplex.echolib.support.fold

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
    private val databaseApiService: DatabaseApiService,
    private val cryptoCoreComponent: CryptoCoreComponent
) : AuthenticationFacade {

    override fun login(name: String, password: String, callback: Callback<Account>) {
        val result = databaseApiService.getFullAccounts(listOf(name), false)

        result.fold({ accountsMap ->
            val foundAccount = accountsMap[name]
            val address = cryptoCoreComponent.getAddress(name, password)
            if (foundAccount != null && isAddressSame(foundAccount, address)) {
                callback.onSuccess(foundAccount)
            } else {
                callback.onError(NotFoundException("Account not found."))
            }
        }, { error ->
            callback.onError(LocalException(error.message, error))
        })
    }

    private fun isAddressSame(account: Account, address: String): Boolean {
        return account.isEqualsByAddress(address, AuthorityType.OWNER)
                || account.isEqualsByAddress(address, AuthorityType.ACTIVE)
    }

    override fun changePassword(
        nameOrId: String,
        oldPassword: String,
        newPassword: String,
        callback: Callback<Account>
    ) {


    }

}
