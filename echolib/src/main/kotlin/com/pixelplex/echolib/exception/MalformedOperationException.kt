package com.pixelplex.echolib.exception

/**
 *  Represents errors associated with illegal AccountUpdateOperation state
 * @see com.pixelplex.echolib.model.operations.AccountUpdateOperation
 *
 * @author Daria Pechkovskaya
 */
class MalformedOperationException: LocalException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}
