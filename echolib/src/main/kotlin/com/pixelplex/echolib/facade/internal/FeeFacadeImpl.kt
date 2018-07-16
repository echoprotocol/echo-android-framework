package com.pixelplex.echolib.facade.internal

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.facade.FeeFacade
import com.pixelplex.echolib.service.DatabaseApiService

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
