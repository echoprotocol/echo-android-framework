package com.pixelplex.echoframework.support.concurrent

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor

/**
 * Executes runnable jobs on main application (UI) thread
 *
 * @author Dmitriy Bushuev
 */
class MainThreadExecutor : Executor {

    private val mainThreadHandler = Handler(Looper.getMainLooper())

    override fun execute(job: Runnable) {
        mainThreadHandler.post(job)
    }

}
