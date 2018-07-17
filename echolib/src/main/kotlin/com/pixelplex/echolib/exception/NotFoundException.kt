package com.pixelplex.echolib.exception

/**
 * Represents errors indicating a resource requested was not found.
 *
 * @author Daria Pechkovskaya
 */
class NotFoundException : LocalException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}

