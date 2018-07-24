package com.pixelplex.echoframework.support.concurrent

import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Implementation of [Dispatcher] based on [ExecutorService]
 *
 * @author Dmitriy Bushuev
 */
class ExecutorServiceDispatcher : Dispatcher {

    // Thread count?
    private val executorService: ExecutorService by lazy {
        ThreadPoolExecutor(
            0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
            SynchronousQueue<Runnable>()
        )
    }

    override fun dispatch(job: Runnable) =
        executorService.execute(job)

}
