package org.echo.mobile.framework.facade

import org.echo.mobile.framework.Callback

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
