package org.echo.mobile.framework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.support.toJsonObject
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
    val signatures: ArrayList<ByteArray>,
    callId: Int,
    callback: Callback<Boolean>
) : SocketOperation<Boolean>(SocketMethodType.CALL, callId, Boolean::class.java, callback) {

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
        val signaturesJson = JsonArray().apply {
            signatures.forEach { add(Hex.toHexString(it)) }
        }
        transactionJson.add(Transaction.KEY_SIGNATURES, signaturesJson)

        return transactionJson
    }

    override fun fromJson(json: String): Boolean? {
        return json.toJsonObject()?.has(RESULT_KEY) ?: false
    }

}
