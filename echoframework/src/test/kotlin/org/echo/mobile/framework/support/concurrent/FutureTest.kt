package org.echo.mobile.framework.support.concurrent

import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.support.concurrent.future.FutureCallback
import org.echo.mobile.framework.support.concurrent.future.FutureTask
import org.echo.mobile.framework.support.concurrent.future.wrapResult
import org.echo.mobile.framework.support.fold
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread

/**
 * Test cases for [FutureTask]
 *
 * @author Dmitriy Bushuev
 */
class FutureTest {

    @Test
    fun callbackTest() {
        val semaphore = Semaphore(0)

        val future = FutureTask<Boolean>()

        thread {
            Thread.sleep(100)
            future.setComplete(true)
        }

        future.setCallback(object : FutureCallback<Boolean> {

            override fun onCompleted(e: Exception?, result: Boolean?) {
                semaphore.release()
            }

        })

        assertTrue(semaphore.tryAcquire(400, TimeUnit.MILLISECONDS))
    }

    @Test
    fun callbackThreadTest() {
        val semaphore = Semaphore(0)

        val future = FutureTask<Boolean>()

        thread {
            Thread.sleep(100)
            future.setComplete(true)
        }

        val mainThreadId = Thread.currentThread().id

        future.setCallback(object : FutureCallback<Boolean> {

            override fun onCompleted(e: Exception?, result: Boolean?) {
                assertNotEquals(mainThreadId, Thread.currentThread().id)
                semaphore.release()
            }

        })

        assertTrue(semaphore.tryAcquire(400, TimeUnit.MILLISECONDS))
    }

    @Test(expected = LocalException::class)
    fun cancelTest() {
        val future = FutureTask<Boolean>()

        thread {
            Thread.sleep(100)
            future.cancel()
        }

        future.get()
    }

    @Test
    fun getResultTest() {
        val result = 10
        val future = FutureTask<Int>()

        thread {
            Thread.sleep(100)
            future.setComplete(result)
        }

        assertEquals(future.get(), result)
    }

    @Test(expected = TimeoutException::class)
    fun timeoutTest() {
        val future = FutureTask<Int>()

        thread {
            Thread.sleep(400)
            future.setComplete(1)
        }

        future.get(50, TimeUnit.MILLISECONDS)
    }

    @Test
    fun futureResultSuccessWrapTest() {
        val result = 10
        val future = FutureTask<Int>()

        thread {
            Thread.sleep(100)
            future.setComplete(result)
        }

        future.wrapResult<Exception, Int>().fold({ received ->
            assertEquals(received, result)
        }, {
            fail()
        })
    }

    @Test
    fun futureResultErrorWrapTest() {
        val future = FutureTask<Int>()

        thread {
            Thread.sleep(100)
            future.setComplete(IllegalStateException())
        }

        future.wrapResult<Exception, Int>().fold({
            fail()
        }, { error ->
            assertTrue(error is LocalException)
            assertTrue(error.cause is IllegalStateException)
        })
    }

}
