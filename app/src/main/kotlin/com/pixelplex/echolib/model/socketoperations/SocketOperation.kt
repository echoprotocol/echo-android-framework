package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.model.JsonSerializable

/**
 * Includes classes for working with blockchain calls
 *
 * @author Daria Pechkovskaya
 */

/**
 * Method type for call to blockchain
 *
 */
enum class SocketMethodType(val key: String) {
    CALL("call")
}

/**
 * Represents blockchain operations by keys
 */
enum class SocketOperationKeys(val key: String) {
    BLOCK_DATA("get_dynamic_global_properties"),
    FULL_ACCOUNTS("get_full_accounts"),
    ACCOUNT_HISTORY("get_account_history"),
    ASSETS("get_assets"),
    BLOCK("get_block"),
    CHAIN_ID("get_chain_id"),
    CONTRACT_RESULT("get_contract_result"),
    OBJECTS("get_objects"),
    KEY_REFERENCES("get_key_references"),
    REQUIRED_FEES("get_required_fees"),
    SUBSCRIBE_CALLBACK("set_subscribe_callback"),
    TRANSACTION_WITH_CALLBACK("broadcast_transaction_with_callback");

    override fun toString(): String = this.key
}

/**
 * Represents result of blockchain call
 */
data class OperationResult<T>(val operation: SocketOperation, val callback: Callback<T>)

/**
 * Keys for json creation of blockchain call
 */
enum class OperationCodingKeys(val key: String) {
    ID("id"),
    METHOD("method"),
    PARAMS("params")
}

/**
 * Represents blockchain call
 * <a href="http://docs.bitshares.org/api/websocket.html">Source</a>
 */
abstract class SocketOperation(
    val method: SocketMethodType,
    val callId: Int,
    val apiId: Int,
    val result: OperationResult<*>
) : JsonSerializable {

    /**
     * Creates json of call parameters
     * @return JsonObject representation
     */
    abstract fun createParameters(): JsonElement

    override fun toJsonString(): String? =
        toJsonObject().toString()

    override fun toJsonObject(): JsonElement =
        JsonObject().apply {
            addProperty(OperationCodingKeys.ID.key, callId)
            addProperty(OperationCodingKeys.METHOD.key, method.key)
            add(OperationCodingKeys.PARAMS.key, createParameters())
        }
}
