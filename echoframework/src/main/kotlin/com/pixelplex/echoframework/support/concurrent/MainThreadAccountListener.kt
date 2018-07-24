package com.pixelplex.echoframework.support.concurrent

import com.pixelplex.echoframework.AccountListener
import com.pixelplex.echoframework.model.Account

/**
 * Executes account listener events on main (UI) thread
 *
 * @author Dmitriy Bushuev
 */
class MainThreadAccountListener(private val delegate: AccountListener) : AccountListener {

    private val mainThreadExecutor =
        MainThreadExecutor()

    override fun onChange(updatedAccount: Account) {
        mainThreadExecutor.execute {
            delegate.onChange(updatedAccount)
        }
    }

}
