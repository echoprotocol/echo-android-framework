package org.echo.mobile.framework.exception

/**
 * Represents exceptions, associated with account authentication.
 *
 * @author Daria Pechkovskaya
 */
class AuthenticationException : LocalException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : this(cause?.message, cause)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}