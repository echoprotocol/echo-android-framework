package com.pixelplex.echolib.support.concurrent

import android.os.Handler
import java.util.concurrent.Executor

/**
 * Executes runnable jobs on thread, originally created this executor
 *
 * @author Dmitriy Bushuev
 */
class OriginalThreadExecutor : Executor {

    private val originalThreadHandler = Handler()

    override fun execute(job: Runnable) {
        originalThreadHandler.post(job)
    }

}
