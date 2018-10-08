package org.echo.mobile.framework.facade

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.FullAccount

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
    fun isOwnedBy(name: String, password: String, callback: Callback<FullAccount>)

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
