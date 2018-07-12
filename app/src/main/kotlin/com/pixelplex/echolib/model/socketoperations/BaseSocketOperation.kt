package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement

/**
 * Represents base blockchain call
 *
 * @author Daria Pechkovskaya
 */
class BaseSocketOperation(
    method: SocketMethodType = SocketMethodType.CALL,
    callId: Int,
    apiId: Int,
    result: OperationResult<*>
) : SocketOperation(method, callId, apiId, result) {

    override fun createParameters(): JsonElement = JsonArray()

}
