package com.pixelplex.echolib.exception

/**
 * Represents errors associated with operations over different entities
 *
 * @author Dmitriy Bushuev
 * @author Darya
 */
class IncompatibleOperationException : RuntimeException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}
