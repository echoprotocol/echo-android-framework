package com.pixelplex.echolib.facade.internal

import com.pixelplex.echolib.AccountListener
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.facade.SubscriptionFacade
import com.pixelplex.echolib.service.NetworkBroadcastApiService

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
    private val networkBroadcastApiService: NetworkBroadcastApiService
) : SubscriptionFacade {

    override fun subscribeOnAccount(nameOrId: String, listener: AccountListener) {
    }

    override fun unsubscribeFromAccount(nameOrId: String, callback: Callback<Boolean>) {
    }

    override fun unsubscribeAll(callback: Callback<Boolean>) {
    }

}
