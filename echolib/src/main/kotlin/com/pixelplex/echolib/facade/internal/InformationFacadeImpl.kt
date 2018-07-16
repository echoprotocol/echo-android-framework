package com.pixelplex.echolib.facade.internal

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.facade.InformationFacade
import com.pixelplex.echolib.model.Account
import com.pixelplex.echolib.model.Balance
import com.pixelplex.echolib.service.DatabaseApiService

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
    }

    override fun checkAccountIsUnavailable(nameOrId: String, callback: Callback<Boolean>) {
    }

    override fun getBalance(nameOrId: String, asset: String, callback: Callback<Balance>) {
    }

}
