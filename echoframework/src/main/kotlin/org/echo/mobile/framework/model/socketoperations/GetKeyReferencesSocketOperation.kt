package org.echo.mobile.framework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.network.Network

/**
 * This function fetches all relevant [Account] objects for the given public [keys].
 *
 * @param keys Keys list that associated with specific account (accounts)
 *
 * @author Daria Pechkovskaya
 */
class GetKeyReferencesSocketOperation(
    override val apiId: Int,
    val keys: List<String>,
    val network: Network,
    callId: Int,
    callback: Callback<Map<String, List<String>>>
) : SocketOperation<Map<String, List<String>>>(
    SocketMethodType.CALL,
    callId,
    mapOf<String, List<String>>().javaClass,
    callback
) {

    @Suppress("UNUSED_EXPRESSION")
    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.KEY_REFERENCES.key)

            val dataJson = JsonArray()

            val idsJson = JsonArray()
            keys.forEach { item -> idsJson.add(item) }
            dataJson.add(idsJson)

            add(JsonArray().apply { addAll(dataJson) })
        }

    override fun fromJson(json: String): Map<String, List<String>> {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        val accountsKeysToIds = hashMapOf<String, List<String>>()

        if (!jsonTree.isJsonObject || jsonTree.asJsonObject.get(RESULT_KEY) == null) {
            return accountsKeysToIds
        }

        fillAccountsMap(jsonTree, accountsKeysToIds)

        return accountsKeysToIds
    }

    private fun fillAccountsMap(json: JsonElement, source: MutableMap<String, List<String>>) {
        try {
            val result = json.asJsonObject.get(RESULT_KEY)?.asJsonArray
            val size = result?.size() ?: 0

            for (i in 0 until size) {
                val keyToAccountIds = result!!.get(i).asJsonArray.map { it.asString }
                source[keys[i]] = keyToAccountIds.toList()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

}
