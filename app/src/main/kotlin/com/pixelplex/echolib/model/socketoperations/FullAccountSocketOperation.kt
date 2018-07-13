package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.model.FullUserAccount
import com.pixelplex.echolib.support.Api
import com.pixelplex.echolib.support.getId

/**
 * This function fetches all relevant [FullUserAccount] objects for the given accounts, and
 * subscribes to updates to the given accounts. If any of the strings in [namesOrIds] cannot be
 * tied to an account, that input will be ignored. All other accounts will be retrieved and
 * subscribed.
 *
 * @param namesOrIds Each item must be the name or ID of an account to retrieve
 * @param shouldSubscribe Flag of subscription on updates
 *
 * @author Daria Pechkovskaya
 */
class FullAccountSocketOperation(
    val api: Api,
    val namesOrIds: Array<String>,
    val shouldSubscribe: Boolean,
    method: SocketMethodType = SocketMethodType.CALL,
    callId: Int,
    callback: Callback<List<FullUserAccount>>
) : SocketOperation<List<FullUserAccount>>(
    method,
    callId,
    listOf<FullUserAccount>().javaClass,
    callback
) {

    @Suppress("UNUSED_EXPRESSION")
    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.FULL_ACCOUNTS.key)

            val dataJson = JsonArray()

            val idsJson = JsonArray()
            namesOrIds.forEach { item -> idsJson.add(item) }
            dataJson.add(idsJson)
            dataJson.add(shouldSubscribe)

            add(JsonArray().apply { dataJson })
        }

    override val apiId: Int
        get() = api.getId()
}
