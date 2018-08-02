package com.pixelplex.echoframework.support.concurrent.future

/**
 * Base realization of [Cancellable]
 *
 * @author Dmitriy Bushuev
 */
open class CancellableTask : Cancellable {

    @Volatile
    private var complete: Boolean = false
    @Volatile
    private var cancelled: Boolean = false

    override fun isDone(): Boolean = complete

    override fun isCancelled(): Boolean = cancelled

    /**
     * Completes current cancellable instance
     *
     * If cancellable is already canceled - return false,
     * if already done - throw exception
     */
    open fun setComplete(): Boolean {
        synchronized(this) {
            if (isCancelled()) {
                return false
            }

            complete = true
        }
        return true
    }

    override fun cancel(): Boolean {
        synchronized(this) {
            if (isDone()) {
                return false
            }

            if (isCancelled()) {
                return true
            }

            cancelled = true
        }

        return true
    }

    /**
     * Resets state of all cancellable flags to default values
     */
    open fun reset(): Cancellable {
        cancel()
        complete = false
        cancelled = false
        return this
    }

}
