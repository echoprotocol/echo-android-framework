package com.pixelplex.echolib.facade

import com.pixelplex.echolib.Callback

/**
 * Encapsulates logic, associated with fee configuration processes
 *
 * @author Dmitriy Bushuev
 */
interface FeeFacade {

    /**
     * Describes defining transfer's operation fee contract
     *
     * @param fromNameOrId Source account name or id
     * @param toNameOrId Target account name or id
     * @param asset Specific asset type id
     * @param callback Listener of operation results
     */
    fun getFeeForTransferOperation(
        fromNameOrId: String,
        toNameOrId: String,
        asset: String,
        callback: Callback<String>
    )

}
