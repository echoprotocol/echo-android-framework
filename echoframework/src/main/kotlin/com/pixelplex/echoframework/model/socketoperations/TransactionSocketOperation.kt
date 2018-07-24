package com.pixelplex.echoframework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.ILLEGAL_ID
import com.pixelplex.echoframework.model.Transaction
import org.spongycastle.util.encoders.Hex

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
    val signature: ByteArray,
    method: SocketMethodType = SocketMethodType.CALL,
    callback: Callback<String>
) : SocketOperation<String>(method, ILLEGAL_ID, String::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.TRANSACTION_WITH_CALLBACK.key)
            add(JsonArray().apply {
                add(callId)
                add(transaction.jsonWithSignature())
            })
        }

    private fun Transaction.jsonWithSignature(): JsonElement {
        val transactionJson = this.toJsonObject().asJsonObject
        val signaturesJson = JsonArray().apply { add(Hex.toHexString(signature)) }
        transactionJson.add(Transaction.KEY_SIGNATURES, signaturesJson)

        return transactionJson
    }

    override fun fromJson(json: String): String? {
        return json
    }

}
