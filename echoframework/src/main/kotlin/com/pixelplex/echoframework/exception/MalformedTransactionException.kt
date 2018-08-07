package com.pixelplex.echoframework.exception

/**
 * Represents errors associated with illegal Transaction state
 * [com.pixelplex.echoframework.model.Transaction]
 *
 * @author Daria Pechkovskaya
 */
class MalformedTransactionException: LocalException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}
