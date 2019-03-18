package org.echo.mobile.framework.service

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.socketoperations.CustomOperation
import org.echo.mobile.framework.support.Result

/**
 *  Encapsulates logic, associated with emitting custom calls to blockchain API
 *
 *  @author Daria Pechkovskaya
 */
interface CustomOperationService {

    /**
     * Calls custom [operation] to blockchain API, returns result to [callback]
     */
    fun <T> callCustomOperation(operation: CustomOperation<T>, callback: Callback<T>)

    /**
     * Calls custom [operation] to blockchain API, returns [Result] of call
     */
    fun <T> callCustomOperation(operation: CustomOperation<T>): Result<LocalException, T>

}
