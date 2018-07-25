package com.pixelplex.echoframework.facade

import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.model.Account

/**
 * Encapsulates logic, associated with user authentication and account configuration processes
 *
 * @author Dmitriy Bushuev
 */
interface AuthenticationFacade {

    /**
     * Authenticate user with defined [name] and [password] parameters. Returns account if exists
     *
     * @param name Account name
     * @param password Required account's password
     * @param callback Listener of login operation results
     */
    fun isOwnedBy(name: String, password: String, callback: Callback<Account>)

    /**
     * Describes password changing logic contract
     *
     * @param name Account name
     * @param oldPassword Current account's password
     * @param newPassword New password that user wants to apply
     * @param callback Listener of operation results
     */
    fun changePassword(
        name: String,
        oldPassword: String,
        newPassword: String,
        callback: Callback<Any>
    )

}
