package org.echo.mobile.framework.support.concurrent

import org.echo.mobile.framework.service.UpdateListener

/**
 * Executes typed update listener events on main (UI) thread
 *
 * @author Dmitriy Bushuev
 */
class MainThreadUpdateListener<T>(private val delegate: UpdateListener<T>) : UpdateListener<T> {

    private val mainThreadExecutor = MainThreadExecutor()

    override fun onUpdate(data: T) {
        mainThreadExecutor.execute {
            delegate.onUpdate(data)
        }
    }

}
