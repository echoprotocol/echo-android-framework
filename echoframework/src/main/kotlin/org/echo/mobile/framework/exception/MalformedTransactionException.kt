package org.echo.mobile.framework.exception

/**
 * Represents errors associated with illegal Transaction state
 * [org.echo.mobile.framework.model.Transaction]
 *
 * @author Daria Pechkovskaya
 */
class MalformedTransactionException : LocalException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}
