package com.pixelplex.echoframework.exception

/**
 * Represents exception for local library usage
 *
 * @author Daria Pechkovskaya
 */
open class LocalException : RuntimeException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}
