package com.pixelplex.echolib.facade

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.support.model.Api

/**
 * Encapsulates logic, associated with socket and blockchain apis connection
 *
 * @author Daria Pechkovskaya
 */
interface InitializerFacade {

    /**
     * Connects to socket and blockchain apis
     */
    fun connect(url: String, apis: Set<Api>, callback: Callback<Any>)

    companion object {
        const val INITIALIZER_API_ID = 1
        const val ILLEGAL_API_ID = -1

        var databaseApiId: Int = ILLEGAL_API_ID
        var accountHistoryApiId: Int = ILLEGAL_API_ID
        var cryptoApiId: Int = ILLEGAL_API_ID
        var networkBroadcastApiId: Int = ILLEGAL_API_ID
        var networkNodesApiId: Int = ILLEGAL_API_ID
    }
}
