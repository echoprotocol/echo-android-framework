package com.pixelplex.echolib.facade.internal

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.exception.LocalException
import com.pixelplex.echolib.exception.NotFoundException
import com.pixelplex.echolib.facade.InformationFacade
import com.pixelplex.echolib.model.Account
import com.pixelplex.echolib.model.Balance
import com.pixelplex.echolib.service.DatabaseApiService
import com.pixelplex.echolib.support.fold

/**
 * Implementation of [InformationFacade]
 *
 * <p>
 *     Delegates API call logic to [DatabaseApiService]
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class InformationFacadeImpl(private val databaseApiService: DatabaseApiService) :
    InformationFacade {

    override fun getAccount(nameOrId: String, callback: Callback<Account>) {
        val result = databaseApiService.getFullAccounts(listOf(nameOrId), false)

        result.fold({ accountMap ->
            val requiredAccount = accountMap[nameOrId]
            requiredAccount?.let { account ->
                callback.onSuccess(account)
            } ?: callback.onError(NotFoundException("Account not found."))
        }, { error ->
            callback.onError(LocalException(error.message, error))
        })
    }

    override fun checkAccountIsUnavailable(nameOrId: String, callback: Callback<Boolean>) {
    }

    override fun getBalance(nameOrId: String, asset: String, callback: Callback<Balance>) {
    }

}
