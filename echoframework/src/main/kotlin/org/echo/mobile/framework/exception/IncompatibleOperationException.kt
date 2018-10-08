package org.echo.mobile.framework.exception

/**
 * Represents errors associated with operations over different entities
 *
 * @author Dmitriy Bushuev
 * @author Daria Pechkovskaya
 */
class IncompatibleOperationException : LocalException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}
