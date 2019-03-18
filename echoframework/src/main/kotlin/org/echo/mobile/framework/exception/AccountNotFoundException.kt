package org.echo.mobile.framework.exception

/**
 * Represents errors indicating an account requested from client was not found on server.
 *
 * @author Daria Pechkovskaya
 */
class AccountNotFoundException : LocalException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}

