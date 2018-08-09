package com.pixelplex.echoframework.exception

/**
 * Represents errors associated with socket connection
 *
 * @author Daria Pechkovskaya
 */
class SocketConnectionException : LocalException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
