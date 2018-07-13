package com.pixelplex.echolib.exception

/**
 * Represents errors associated with illegal Transaction state
 * (@see com.pixelplex.echolib.model.Transaction)
 *
 * @author Daria Pechkovskaya
 */
class MalformedTransactionException: LocalException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}
