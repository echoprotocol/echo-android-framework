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

    /**
     * Disconnects socket
     */
    fun disconnect()
}
