package org.echo.mobile.framework.service

import org.echo.mobile.framework.Callback

/**
 * Encapsulates logic, associated with blockchain registration API
 *
 * @author Daria Pechkovskaya
 */
interface RegistrationApiService : ApiService {

    /**
     * Registration on blockchain node.
     *
     * @param callback Listener of call result. Return true if call succeed, otherwise false
     */
    fun register(
        accountName: String,
        keyOwner: String,
        keyActive: String,
        keyMemo: String,
        echorandKey: String,
        callback: Callback<Boolean>
    )
}
