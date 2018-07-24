package com.pixelplex.echoframework

import com.pixelplex.echoframework.model.Account

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
    fun onChange(updatedAccount: Account)

}
