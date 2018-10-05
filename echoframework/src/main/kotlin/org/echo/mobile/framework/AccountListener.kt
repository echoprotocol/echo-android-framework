package org.echo.mobile.framework

import org.echo.mobile.framework.model.FullAccount

/**
 * Account subscription results listener
 *
 * @author Dmitriy Bushuev
 */
interface AccountListener {

    /**
     * Calls when observing account changed
     *
     * @param updatedAccount Updated account with all changes
     */
    fun onChange(updatedAccount: FullAccount)

}
