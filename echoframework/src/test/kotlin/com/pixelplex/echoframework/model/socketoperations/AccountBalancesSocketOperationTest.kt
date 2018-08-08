package com.pixelplex.echoframework.model.socketoperations

import com.pixelplex.echoframework.support.EmptyCallback
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test cases for [AccountBalancesOperation]
 *
 * @author Dmitriy Bushuev
 */
class AccountBalancesSocketOperationTest {

    private lateinit var operation: AccountBalancesOperation

    @Test
    fun serializeTest() {
        operation = AccountBalancesOperation(
            2,
            "1.2.23215",
            "1.3.0",
            false,
            3,
            callback = EmptyCallback()
        )
        val json = operation.toJsonObject().asJsonObject

        assertEquals(json.get(OperationCodingKeys.ID.key).asInt, 3)
        assertEquals(
            json.get(OperationCodingKeys.METHOD.key).asString,
            SocketMethodType.CALL.key
        )

        val parameters = json.getAsJsonArray(OperationCodingKeys.PARAMS.key)
        assertTrue(parameters.size() == 3)

        val apiId = parameters[0].asInt
        assertEquals(apiId, 2)

        val apiName = parameters[1].asString
        assertEquals(apiName, SocketOperationKeys.ACCOUNT_BALANCES.key)

        val requestParameters = parameters[2].asJsonArray
        assertTrue(requestParameters.size() == 3)

        val accountId = requestParameters[0].asString
        assertEquals(accountId, ACCOUNT_ID)

        val assets = requestParameters[1].asJsonArray
        assertTrue(assets.size() == 1)
    }

    companion object {
        private const val ACCOUNT_ID = "1.2.23215"
    }

}
