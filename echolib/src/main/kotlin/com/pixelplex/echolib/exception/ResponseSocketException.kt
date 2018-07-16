package com.pixelplex.echolib.exception

/**
 * Represents errors associated with error response from socket
 *
 * @author Daria Pechkovskaya
 */
class ResponseSocketException : LocalException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}
