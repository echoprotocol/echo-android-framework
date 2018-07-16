package com.pixelplex.echolib.facade

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.support.Api

/**
 * Encapsulates logic, associated with socket and blockchain apis connection
 *
 * @author Daria Pechkovskaya
 */
interface InitializerFacade {

    /**
     * Connects to socket and blockchain apis
     */
    fun connect(callback: Callback<Any>)

    companion object {
        /**
         * Blockchain api id for initializing another apis
         */
        const val INITIALIZER_API_ID = 1

        const val ILLEGAL_API_ID = -1

        /**
         * Actual id for DatabaseApi
         */
        @Volatile
        var databaseApiId: Int = ILLEGAL_API_ID

        /**
         * Actual id for AccountHistoryApi
         */
        @Volatile
        var accountHistoryApiId: Int = ILLEGAL_API_ID

        /**
         * Actual id for CryptoApi
         */
        @Volatile
        var cryptoApiId: Int = ILLEGAL_API_ID

        /**
         * Actual id for NetworkBroadcastApi
         */
        @Volatile
        var networkBroadcastApiId: Int = ILLEGAL_API_ID

        /**
         * Actual id for NetworkNodesApi
         */
        @Volatile
        var networkNodesApiId: Int = ILLEGAL_API_ID
    }
}
