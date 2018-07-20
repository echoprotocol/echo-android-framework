package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.ILLEGAL_ID
import com.pixelplex.echolib.model.Transaction

/**
 * Broadcast a transaction to the network.
 * The [transaction] will be checked for validity in the local database prior to broadcasting. If it
 * fails to apply locally, an error will be thrown and the transaction will not be broadcast.
 *
 * @param transaction Transaction for broadcast
 *
 * @author Daria Pechkovskaya
 */
class TransactionSocketOperation(
    override val apiId: Int,
    val transaction: Transaction,
    method: SocketMethodType = SocketMethodType.CALL,
    callback: Callback<String>
) : SocketOperation<String>(method, ILLEGAL_ID, String::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.TRANSACTION_WITH_CALLBACK.key)
            add(JsonArray().apply {
                add(callId)
                add(transaction.toJsonObject())
            })
        }

    override fun fromJson(json: String): String? {
        return json
    }
}
