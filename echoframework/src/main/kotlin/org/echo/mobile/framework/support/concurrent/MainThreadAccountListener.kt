package org.echo.mobile.framework.support.concurrent

import org.echo.mobile.framework.AccountListener
import org.echo.mobile.framework.model.FullAccount

/**
 * Executes account listener events on main (UI) thread
 *
 * @author Dmitriy Bushuev
 */
class MainThreadAccountListener(private val delegate: AccountListener) : AccountListener {

    private val mainThreadExecutor = MainThreadExecutor()

    override fun onChange(updatedAccount: FullAccount) {
        mainThreadExecutor.execute {
            delegate.onChange(updatedAccount)
        }
    }

}
