package com.pixelplex.echoframework.support.concurrent.future

/**
 * Describe base functionality of simple cancellable process wrapper
 *
 * @author Dmitriy Bushuev
 */
interface Cancellable {

    /**
     * Check whether this asynchronous operation completed successfully.
     */
    fun isDone(): Boolean

    /**
     * Check whether this asynchronous operation has been cancelled.
     */
    fun isCancelled(): Boolean

    /**
     * Attempt to cancel this asynchronous operation.
     *
     * @return The return value is whether the operation cancelled successfully.
     */
    fun cancel(): Boolean

}
