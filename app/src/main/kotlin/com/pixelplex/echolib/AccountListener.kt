package com.pixelplex.echolib

import com.pixelplex.echolib.model.Account

/**
 * Account subscription results listener
 *
 * @author Dmitriy Bushuev
 */
interface AccountListener {

    /**
     * Calls when observing account change
     *
     * @param updatedAccount Updated account with all changes
     */
    fun onChange(updatedAccount: Account)

}
