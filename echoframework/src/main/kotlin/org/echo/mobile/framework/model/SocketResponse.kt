package org.echo.mobile.framework.model

import com.google.gson.annotations.Expose

/**
 * Includes classes, represents response with error from blockchain
 *
 * @author Daria Pechkovskaya
 */

/**
 * Represents response from blockchain
 */
open class SocketResponse(
    @Expose val id: Int,
    @Expose val error: Error?
)

/**
 * Represents typed response with result from blockchain
 */
class SocketResponseResult<T>(
    id: Int,
    error: Error?,
    @Expose val result: T
) : SocketResponse(id, error)

/**
 * Represents error from blockchain response
 */
data class Error(
    @Expose val code: Int,
    @Expose val data: ErrorData,
    @Expose val message: String
)

/**
 * Represents error data from blockchain response
 */
data class ErrorData(
    @Expose val code: Int,
    @Expose val name: String,
    @Expose val message: String
)
