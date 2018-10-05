package com.pixelplex.echoframework.model.socketoperations

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.model.JsonDeserializable
import com.pixelplex.echoframework.model.JsonSerializable

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
    CALL("call"),
    NOTICE("notice")
}

/**
 * Represents blockchain operations by keys
 */
enum class SocketOperationKeys(val key: String) {
    LOGIN("login"),
    BLOCK_DATA("get_dynamic_global_properties"),
    FULL_ACCOUNTS("get_full_accounts"),
    ACCOUNT_BALANCES("get_account_balances"),
    ACCOUNT_HISTORY("get_account_history"),
    ASSETS("get_assets"),
    LIST_ASSETS("list_assets"),
    BLOCK("get_block"),
    CHAIN_ID("get_chain_id"),
    CONTRACT_RESULT("get_contract_result"),
    OBJECTS("get_objects"),
    KEY_REFERENCES("get_key_references"),
    REQUIRED_FEES("get_required_fees"),
    SUBSCRIBE_CALLBACK("set_subscribe_callback"),
    SET_BLOCK_APPLIED_CALLBACK("set_block_applied_callback"),
    CANCEL_ALL_SUBSCRIPTIONS("cancel_all_subscriptions"),
    TRANSACTION_WITH_CALLBACK("broadcast_transaction_with_callback"),
    GET_ALL_CONTRACTS("get_all_contracts"),
    GET_CONTRACT_RESULT("get_contract_result"),
    CALL_CONTRACT_NO_CHANGING_STATE("call_contract_no_changing_state"),
    GET_CONTRACTS("get_contracts"),
    GET_CONTRACT("get_contract");

    override fun toString(): String = this.key
}

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
 * <a href="http://docs.bitshares.org/api/rpc.html">Source</a>
 */
abstract class SocketOperation<T>(
    val method: SocketMethodType,
    val callId: Int,
    val type: Class<T>,
    val callback: Callback<T>
) : JsonSerializable, JsonDeserializable<T> {

    /**
     * Creates json of call parameters
     * @return JsonObject representation
     */
    abstract fun createParameters(): JsonElement

    /**
     * Blockchain api id, which contains operation
     */
    abstract val apiId: Int

    override fun toJsonString(): String? =
        toJsonObject().toString()

    override fun toJsonObject(): JsonElement =
        JsonObject().apply {
            addProperty(OperationCodingKeys.ID.key, callId)
            addProperty(OperationCodingKeys.METHOD.key, method.key)
            add(OperationCodingKeys.PARAMS.key, createParameters())
        }

    companion object {
        const val RESULT_KEY = "result"
    }
}
