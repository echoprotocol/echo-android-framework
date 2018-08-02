package com.pixelplex.echoframework.exception

/**
 * Represents errors associated with illegal AccountUpdateOperation state
 * [com.pixelplex.echoframework.model.operations.AccountUpdateOperation]
 *
 * @author Daria Pechkovskaya
 */
class MalformedOperationException: LocalException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}
