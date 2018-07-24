package com.pixelplex.echoframework.exception

/**
 * Represents errors indicating a account requested from client was not found on server.
 *
 * @author Daria Pechkovskaya
 */
class NotFoundException : LocalException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}

