package org.echo.mobile.framework.exception

/**
 * Represents errors associated with illegal Address state
 * [org.echo.mobile.framework.model.Address]
 *
 * @author Dmitriy Bushuev
 * @author Daria Pechkovskaya
 */
class MalformedAddressException : LocalException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}
