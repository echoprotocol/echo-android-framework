package com.pixelplex.echoframework.exception

/**
 * Represents errors associated with illegal Address state
 * (@see com.pixelplex.echolib.model.Address)
 *
 * @author Dmitriy Bushuev
 * @author Daria Pechkovskaya
 */
class MalformedAddressException : LocalException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}
