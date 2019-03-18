package org.echo.mobile.framework.support.concurrent.future

import org.echo.mobile.framework.exception.LocalException
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Realization of cancellable future
 *
 * Adds functionality to set completed value or error in future
 *
 * Example:
 *       val future = FutureTask<String>()
 *       lib.start(object : Callback<String> {
 *
 *       override fun onSuccess(result: String) {
 *          future.setComplete(result)
 *       }
 *
 *       override fun onError(error: LocalException) {
 *          future.setComplete(error)
 *       }
 *
 *       })
 *
 *       // blocking current thread until result or error won't be obtained
 *       val result = future.get()
 *
 * @author Dmitriy Bushuev
 */
class FutureTask<T> : CancellableTask, CancellableFuture<T> {

    private var waiter: Semaphore? = null
    private var exception: Exception? = null
    private var result: T? = null
    private var callback: FutureCallback<T>? = null

    private val completionCallback: FutureCallback<T>
        get() = object : FutureCallback<T> {
            override fun onCompleted(e: Exception?, result: T?) {
                setComplete(e, result)
            }
        }

    constructor()

    constructor(value: T) {
        setComplete(value)
    }

    constructor(e: Exception) {
        setComplete(e)
    }

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        return cancel()
    }

    override fun cancel(): Boolean {
        return cancelInternal()
    }

    private fun cancelInternal(): Boolean {
        if (!super.cancel()) {
            return false
        }

        // still need to release any pending waiters
        var callback: FutureCallback<T>? = null
        synchronized(this) {
            this.exception = CancellationException()
            releaseWaiterLocked()
            callback = handleCompleteLocked()
        }

        handleCallbackUnlocked(callback)
        return true
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    override fun get(): T? {
        var waiter: Semaphore? = null

        synchronized(this) {
            if (isCancelled || isDone)
                return getResultOrThrow()
            waiter = ensureWaiterLocked()
        }

        waiter!!.acquire()

        return getResultOrThrow()
    }

    @Throws(InterruptedException::class, ExecutionException::class, TimeoutException::class)
    override fun get(timeout: Long, unit: TimeUnit): T? {
        var waiter: Semaphore? = null

        synchronized(this) {
            if (isCancelled || isDone)
                return getResultOrThrow()
            waiter = ensureWaiterLocked()
        }

        if (!waiter!!.tryAcquire(timeout, unit))
            throw TimeoutException()

        return getResultOrThrow()
    }

    private fun getResultOrThrow(): T? {
        if (exception != null)
            throw LocalException(exception)
        return result
    }

    override fun setComplete(): Boolean {
        return setComplete(null as T?)
    }

    /**
     * Defines complete value [value] and null error. Completes execution
     */
    fun setComplete(value: T?): Boolean {
        return setComplete(null, value)
    }

    /**
     * Defines complete value [value] and error [error]. Completes execution
     */
    @JvmOverloads
    fun setComplete(error: Exception?, value: T? = null): Boolean {
        var callback: FutureCallback<T>? = null

        synchronized(this) {
            if (!super.setComplete())
                return false
            result = value
            exception = error
            releaseWaiterLocked()
            callback = handleCompleteLocked()
        }

        handleCallbackUnlocked(callback)
        return true
    }

    private fun handleCompleteLocked(): FutureCallback<T>? {
        // don't execute the callback inside the sync block... possible hangup
        // read the callback value, and then call it outside the block.
        // can't simply call this.callback.onCompleted directly outside the block,
        // because that may result in a race condition where the callback changes once leaving
        // the block.
        val callback = this.callback
        // null out members to allow garbage collection
        this.callback = null
        return callback
    }

    private fun handleCallbackUnlocked(callback: FutureCallback<T>?) {
        callback?.onCompleted(exception, result)
    }

    private fun releaseWaiterLocked() {
        if (waiter != null) {
            waiter!!.release()
            waiter = null
        }
    }

    private fun ensureWaiterLocked(): Semaphore {
        if (waiter == null)
            waiter = Semaphore(0)
        return waiter!!
    }

    override fun setCallback(callback: FutureCallback<T>): CancellableFuture<T> {
        // callback can only be changed or read/used inside a sync block
        synchronized(this) {
            this.callback = callback
            if (isDone || isCancelled)
                handleCompleteLocked()
        }

        return this
    }

    override fun <C : FutureCallback<T>> then(callback: C): C {
        setCallback(callback)
        return callback
    }

    /**
     * Reset the future for reuse.
     *
     * @return
     */
    override fun reset(): FutureTask<T> {
        super.reset()

        result = null
        exception = null
        waiter = null
        callback = null

        return this
    }

    override fun tryGetException(): Exception? {
        return exception
    }

    override fun tryGet(): T? {
        return result
    }

}
