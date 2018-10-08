package org.echo.mobile.framework.support.concurrent

/**
 * Encapsulates logic of dispatching asynchronous requests
 *
 * @author Dmitriy Bushuev
 */
interface Dispatcher {

    /**
     * Dispatches required job
     */
    fun dispatch(job: Runnable)

}
