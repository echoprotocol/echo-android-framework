package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.ILLEGAL_ID
import com.pixelplex.echolib.model.DynamicGlobalProperties
import com.pixelplex.echolib.support.Api
import com.pixelplex.echolib.support.getId

/**
 * Returns the block chain’s rapidly-changing properties. The returned object contains information
 * that changes every block interval such as the head block number, the next witness, etc.
 *
 * @return [DynamicGlobalProperties]
 *
 * @author Daria Pechkovskaya
 */
class BlockDataSocketOperation(
    val api: Api,
    method: SocketMethodType = SocketMethodType.CALL,
    callback: Callback<DynamicGlobalProperties>
) : SocketOperation<DynamicGlobalProperties>(
    method,
    ILLEGAL_ID,
    DynamicGlobalProperties::class.java,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.BLOCK_DATA.key)
            add(JsonArray())
        }

    override val apiId: Int
        get() = api.getId()

    override fun fromJson(json: String): DynamicGlobalProperties? {
        return null
    }
}
