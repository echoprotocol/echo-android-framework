package com.pixelplex.bitcoinj

/**
 * Error occurred when expecting private key doesn't exist
 *
 * @author Daria Pechkovskaya
 */
class MissingPrivateKeyException : RuntimeException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}
