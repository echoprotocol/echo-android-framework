package com.pixelplex.echolib.facade.internal

import com.pixelplex.echolib.AccountListener
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.facade.SubscriptionFacade
import com.pixelplex.echolib.service.DatabaseApiService

/**
 * Implementation of [SubscriptionFacade]
 *
 * <p>
 *     Delegates API call logic to [NetworkBroadcastApiService] and [NetworkNodesApiService]
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class SubscriptionFacadeImpl(
    private val databaseApiService: DatabaseApiService
) : SubscriptionFacade {

    override fun subscribeOnAccount(id: String, listener: AccountListener) {
        databaseApiService.subscribeOnAccount(id, listener)
    }

    override fun unsubscribeFromAccount(id: String, callback: Callback<Boolean>) {
        databaseApiService.unsubscribeFromAccount(id, callback)
    }

    override fun unsubscribeAll(callback: Callback<Boolean>) {
        databaseApiService.unsubscribeAll(callback)
    }

}
