package org.echo.mobile.framework.support.concurrent

import org.echo.mobile.framework.support.concurrent.future.FutureTask
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

/**
 * Test cases for [ExecutorServiceDispatcher]
 *
 * @author Dmitriy Bushuev
 */
class ExecutorServiceDispatcherTest {

    private lateinit var dispatcher: ExecutorServiceDispatcher

    @Before
    fun setUp() {
        dispatcher = ExecutorServiceDispatcher()
    }

    @Test
    fun newThreadDispatchTest() {
        val currentThreadId = Thread.currentThread().id

        val dispatcherThreadIdFuture = FutureTask<Long>()

        dispatcher.dispatch(Runnable {
            dispatcherThreadIdFuture.setComplete(Thread.currentThread().id)
        })

        assertNotEquals(currentThreadId, dispatcherThreadIdFuture.get())
    }

}
