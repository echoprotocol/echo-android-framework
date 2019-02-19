package org.echo.mobile.framework.exception

/**
 * Represents errors associated with illegal AccountUpdateOperation state
 * [org.echo.mobile.framework.model.operations.AccountUpdateOperation]
 *
 * @author Daria Pechkovskaya
 */
class MalformedOperationException : LocalException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}
