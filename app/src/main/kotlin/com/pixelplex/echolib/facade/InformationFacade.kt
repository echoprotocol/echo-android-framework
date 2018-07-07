package com.pixelplex.echolib.facade

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.model.Account
import com.pixelplex.echolib.model.Balance

/**
 * Encapsulates logic, associated with receiving blockchain information processes
 *
 * @author Dmitriy Bushuev
 */
interface InformationFacade {

    /**
     * Describes account search logic contract
     *
     * @param nameOrId Required account name or id
     * @param callback Listener of operation results.
     *                 Receives success result only when required account exists
     */
    fun getAccount(nameOrId: String, callback: Callback<Account>)

    /**
     * Describes account availability checking contract
     *
     * @param nameOrId Required account name or id
     * @param callback Listener of operation results.
     *                 Retrieves true, if account is available, otherwise false
     */
    fun checkAccountIsUnavailable(nameOrId: String, callback: Callback<Boolean>)

    /**
     * Describes account balance retrieving logic contract
     *
     * @param nameOrId Required account name or id
     * @param asset    Specific asset type id
     * @param callback Listener of operation results
     */
    fun getBalance(nameOrId: String, asset: String, callback: Callback<Balance>)

}
