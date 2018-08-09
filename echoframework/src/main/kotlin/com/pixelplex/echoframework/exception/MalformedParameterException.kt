package com.pixelplex.echoframework.exception

/**
 * Represents errors associated with illegal Builder parameter state
 * [com.pixelplex.echoframework.support.Builder]
 *
 * @author Daria Pechkovskaya
 */
class MalformedParameterException: LocalException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}
