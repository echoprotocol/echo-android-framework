package org.echo.mobile.framework.service

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.FullAccount

/**
 * Encapsulates logic, associated with blockchain registration API
 *
 * @author Daria Pechkovskaya
 */
interface RegistrationApiService : ApiService {

    /**
     * Registration on blockchain node.
     *
     * @param callback Listener of call result. Return actual registered account
     */
    fun register(
        accountName: String,
        keyOwner: String,
        keyActive: String,
        keyMemo: String,
        echorandKey: String,
        callback: Callback<FullAccount>
    )
}
