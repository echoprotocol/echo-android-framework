package org.echo.mobile.framework.service

import org.echo.mobile.framework.support.Result

/**
 * Encapsulates logic, associated with blockchain registration API
 *
 * @author Daria Pechkovskaya
 */
interface RegistrationApiService : ApiService {

    /**
     * Registration on blockchain node.
     *
     * @return Call [Result]. Return true if call succeed, otherwise false
     */
    fun register(
        accountName: String,
        keyOwner: String,
        keyActive: String,
        keyMemo: String,
        echorandKey: String
    ): Result<Exception, Int>
}
