package com.pixelplex.echolib.facade.internal

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.core.crypto.CryptoCoreComponent
import com.pixelplex.echolib.exception.LocalException
import com.pixelplex.echolib.exception.NotFoundException
import com.pixelplex.echolib.facade.AuthenticationFacade
import com.pixelplex.echolib.model.Account
import com.pixelplex.echolib.model.AuthorityType
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
    private val databaseApiService: DatabaseApiService,
    private val cryptoCoreComponent: CryptoCoreComponent
) : AuthenticationFacade {

    override fun login(name: String, password: String, callback: Callback<Account>) {
        databaseApiService.getFullAccounts(
            listOf(name),
            false,
            object : Callback<Map<String, Account>> {
                override fun onSuccess(result: Map<String, Account>) {
                    if (result.isNotEmpty()) {
                        val foundAccount = result[name]

                        val address = cryptoCoreComponent.getAddress(name, password)
                        if (foundAccount != null && isAddressSame(foundAccount, address)) {
                            callback.onSuccess(foundAccount)
                            return
                        }
                    }

                    callback.onError(NotFoundException("Account not found."))
                }

                override fun onError(error: LocalException) {
                    callback.onError(error)
                }
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
