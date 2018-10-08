package org.echo.mobile.framework.exception

/**
 * Represents exception for local library usage
 *
 * @author Daria Pechkovskaya
 */
open class LocalException : RuntimeException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : this(cause?.message, cause)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}
