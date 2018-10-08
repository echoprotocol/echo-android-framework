package org.echo.mobile.framework.exception

/**
 * Represents errors associated with illegal Builder parameter state
 * [org.echo.mobile.framework.support.Builder]
 *
 * @author Daria Pechkovskaya
 */
class MalformedParameterException: LocalException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}
