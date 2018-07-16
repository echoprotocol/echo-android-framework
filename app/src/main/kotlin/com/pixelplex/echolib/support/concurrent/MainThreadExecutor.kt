package com.pixelplex.echolib.support.concurrent

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor

/**
 * Executes runnable jobs on thread, originally created this executor
 *
 * @author Dmitriy Bushuev
 */
class MainThreadExecutor : Executor {

    private val mainThreadHandler = Handler(Looper.getMainLooper())

    override fun execute(job: Runnable) {
        mainThreadHandler.post(job)
    }

}
