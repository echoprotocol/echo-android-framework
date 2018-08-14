package com.pixelplex.echoframework.model.socketoperations

import com.pixelplex.echoframework.support.EmptyCallback
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * Test cases for [GetAllContractsSocketOperation]
 *
 * @author Daria Pechkovskaya
 */
class GetAllContractsSocketOperationTest {

    private lateinit var operation: GetAllContractsSocketOperation

    private val testApiId = 3
    private val testCallId = 3

    @Before
    fun setUp() {
        operation = GetAllContractsSocketOperation(
            testApiId,
            testCallId,
            callback = EmptyCallback()
        )
    }

    @Test
    fun serializeToJsonTest() {
        val json = operation.toJsonObject().asJsonObject

        Assert.assertEquals(json.get(OperationCodingKeys.ID.key).asInt, testCallId)
        Assert.assertEquals(
            json.get(OperationCodingKeys.METHOD.key).asString,
            SocketMethodType.CALL.key
        )

        val parameters = json.getAsJsonArray(OperationCodingKeys.PARAMS.key)
        Assert.assertTrue(parameters.size() == 3)

        val apiId = parameters[0].asInt
        Assert.assertEquals(apiId, testApiId)

        val apiName = parameters[1].asString
        Assert.assertEquals(apiName, SocketOperationKeys.GET_ALL_CONTRACTS.key)

        val requestParameters = parameters[2].asJsonArray
        Assert.assertTrue(requestParameters.size() == 0)
    }

    @Test
    fun deserializeFromJsonTest() {
        val contracts = operation.fromJson(result)

        assert(contracts.size == 3)

        val contract = contracts[0]
        assertNotNull(contract)
        assertEquals(contract.getObjectId(), "1.16.0")
    }

    val result = """{
                      "id": 5,
                      "jsonrpc": "2.0",
                      "result": [
                        {
                          "id": "1.16.0",
                          "statistics": "2.20.0",
                          "suicided": false
                        },
                        {
                          "id": "1.16.1",
                          "statistics": "2.20.1",
                          "suicided": false
                        },
                        {
                          "id": "1.16.2",
                          "statistics": "2.20.2",
                          "suicided": false
                        }
                      ]
                    }"""
}