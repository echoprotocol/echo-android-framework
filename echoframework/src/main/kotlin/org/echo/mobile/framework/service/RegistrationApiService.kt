package org.echo.mobile.framework.service

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.model.RegistrationTask
import org.echo.mobile.framework.support.Result

/**
 * Encapsulates logic, associated with blockchain registration API
 *
 * @author Daria Pechkovskaya
 */
interface RegistrationApiService : ApiService {

    /**
     * Registration task submitting
     *
     * @return Call [Result]. Returns call id for notice receiving
     */
    fun submitRegistrationSolution(
        accountName: String,
        keyActive: String,
        echorandKey: String,
        nonce: UnsignedLong,
        randNum: UnsignedLong
    ): Result<Exception, Int>

    /**
     * Requests task for solving registration issue
     */
    fun requestRegistrationTask(): Result<Exception, RegistrationTask>
}
