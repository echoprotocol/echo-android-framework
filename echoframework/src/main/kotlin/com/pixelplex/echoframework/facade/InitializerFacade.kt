package com.pixelplex.echoframework.facade

import com.pixelplex.echoframework.Callback

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
    }
}