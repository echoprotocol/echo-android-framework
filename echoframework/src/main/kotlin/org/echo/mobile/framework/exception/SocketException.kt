package org.echo.mobile.framework.exception

/**
 * Represents errors associated with illegal Socket state
 *
 * @author Daria Pechkovskaya
 */
class SocketException : LocalException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}
