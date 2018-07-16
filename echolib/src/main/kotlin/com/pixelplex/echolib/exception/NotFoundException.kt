package com.pixelplex.echolib.exception

/**
 * @author Daria Pechkovskaya
 */
class NotFoundException : LocalException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}