package org.echo.mobile.framework.support.concurrent.future

/**
 * Callback, using when the result of future needed as soon as it's completed or failed
 *
 * @author Dmitriy Bushuev
 */
interface FutureCallback<T> {

    /**
     * Called by the CancellableFuture with the result or exception of the asynchronous operation.
     *
     * @param e Exception encountered by the operation
     * @param result Result returned from the operation
     */
    fun onCompleted(e: Exception?, result: T?)

}
