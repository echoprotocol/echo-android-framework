package com.pixelplex.echolib.facade

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.model.Account

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
    fun login(name: String, password: String, callback: Callback<Account>)

    /**
     * Describes password changing logic contract
     *
     * @param nameOrId Account name or id
     * @param oldPassword Current account's password
     * @param newPassword New password that user wants to apply
     * @param callback Listener of operation results
     */
    fun changePassword(
        nameOrId: String,
        oldPassword: String,
        newPassword: String,
        callback: Callback<Account>
    )

}