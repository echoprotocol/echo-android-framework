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
     * Authenticate user with defined [nameOrId] and [wif] parameters.
     * Returns account if exists and belongs to required user
     *
     * @param nameOrId Account name or id
     * @param wif Required account's wif
     * @param callback Listener of login operation results
     */
    fun isOwnedBy(nameOrId: String, wif: String, callback: Callback<FullAccount>)

    /**
     * Describes wif changing logic contract
     *
     * @param name Account name
     * @param oldWif Current account's wif
     * @param newWif New wif that user wants to apply
     * @param callback Listener of operation results
     */
    fun changeWif(
        name: String,
        oldWif: String,
        newWif: String,
        callback: Callback<Any>
    )

    /**
     * Registers user in echo blockchain using [userName] and already generated [wif]
     *
     * Returns true/false according to registration result
     */
    fun register(userName: String, wif: String, callback: Callback<Boolean>)

}
