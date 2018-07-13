package com.pixelplex.echolib.model

import com.google.gson.annotations.Expose
import java.io.Serializable

/**
 * Includes classes, represents response with error from blockchain
 * <a href="http://docs.bitshares.org/api/rpc.html">Source</a>
 *
 * @author Daria Pechkovskaya
 */

/**
 * Represents response from blockchain
 */
data class SocketResponse(
    @Expose val id: Int,
    @Expose val error: Error?
)

/**
 * Represents error from blockchain response
 */
data class Error(
    @Expose val code: Int,
    @Expose val data: ErrorData
)

/**
 * Represents error data from blockchain response
 */
data class ErrorData(
    @Expose val code: Int,
    @Expose val name: String,
    @Expose val message: String,
    @Expose val stack: List<Serializable>
)
