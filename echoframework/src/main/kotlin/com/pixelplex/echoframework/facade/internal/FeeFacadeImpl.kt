package com.pixelplex.echoframework.facade.internal

import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.facade.FeeFacade
import com.pixelplex.echoframework.service.DatabaseApiService

/**
 * Implementation of [FeeFacade]
 *
 * <p>
 *     Delegates API call logic to [DatabaseApiService]
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class FeeFacadeImpl(private val databaseApiService: DatabaseApiService) : FeeFacade {

    override fun getFeeForTransferOperation(
        fromNameOrId: String,
        toNameOrId: String,
        asset: String,
        callback: Callback<String>
    ) {
    }

}
