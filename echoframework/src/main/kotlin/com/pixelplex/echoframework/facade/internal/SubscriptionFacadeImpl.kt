package com.pixelplex.echoframework.facade.internal

import com.pixelplex.echoframework.AccountListener
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.facade.SubscriptionFacade
import com.pixelplex.echoframework.service.DatabaseApiService

/**
 * Implementation of [SubscriptionFacade]
 *
 * Delegates API call logic to [DatabaseApiService]
 *
 * @author Dmitriy Bushuev
 */
class SubscriptionFacadeImpl(
    private val databaseApiService: DatabaseApiService
) : SubscriptionFacade {

    override fun subscribeOnAccount(
        nameOrId: String,
        listener: AccountListener,
        callback: Callback<Boolean>
    ) = databaseApiService.subscribeOnAccount(nameOrId, listener, callback)

    override fun unsubscribeFromAccount(nameOrId: String, callback: Callback<Boolean>) =
        databaseApiService.unsubscribeFromAccount(nameOrId, callback)

    override fun unsubscribeAll(callback: Callback<Boolean>) =
        databaseApiService.unsubscribeAll(callback)

}
