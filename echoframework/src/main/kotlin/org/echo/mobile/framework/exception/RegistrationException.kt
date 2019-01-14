package org.echo.mobile.framework.exception

/**
 * Represents errors associated with registration flow.
 *
 * @author Daria Pechkovskaya
 */
class RegistrationException : LocalException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}
