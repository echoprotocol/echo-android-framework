package org.echo.mobile.framework.model.socketoperations

import org.echo.mobile.framework.model.network.Testnet
import org.echo.mobile.framework.model.operations.ContractCreateOperation
import org.echo.mobile.framework.model.operations.OperationType
import org.echo.mobile.framework.model.operations.TransferOperation
import org.echo.mobile.framework.support.EmptyCallback
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test cases for [GetAccountHistorySocketOperation]
 *
 * @author Daria Pechkovskaya
 */
class GetAccountHistorySocketOperationTest {

    private lateinit var operation: GetAccountHistorySocketOperation
    private val testApiId = 3
    private val testCallId = 3
    private val testAccountId = "1.2.3"
    private val testStartId = "1.11.0"
    private val testLimit = 10
    private val testStopId = "1.11.0"

    @Before
    fun setUp() {
        operation = GetAccountHistorySocketOperation(
            testApiId,
            testAccountId,
            testStartId,
            testStopId,
            testLimit,
            Testnet(),
            testCallId,
            callback = EmptyCallback()
        )
    }

    @Test
    fun serializeToJsonTest() {
        val json = operation.toJsonObject().asJsonObject

        assertEquals(json.get(OperationCodingKeys.ID.key).asInt, testCallId)
        assertEquals(
            json.get(OperationCodingKeys.METHOD.key).asString,
            SocketMethodType.CALL.key
        )

        val parameters = json.getAsJsonArray(OperationCodingKeys.PARAMS.key)
        assertTrue(parameters.size() == 3)

        val apiId = parameters[0].asInt
        assertEquals(apiId, testApiId)

        val apiName = parameters[1].asString
        assertEquals(apiName, SocketOperationKeys.ACCOUNT_HISTORY.key)

        val requestParameters = parameters[2].asJsonArray
        assertTrue(requestParameters.size() == 4)

        val accountId = requestParameters[0].asString
        assertEquals(accountId, testAccountId)

        val startId = requestParameters[1].asString
        assertEquals(startId, testStartId)

        val limit = requestParameters[2].asInt
        assert(limit == testLimit)

        val stopId = requestParameters[1].asString
        assertEquals(stopId, testStopId)
    }

    @Test
    fun deserializeFromJsonTest() {
        val history = operation.fromJson(result)

        assertNotNull(history)
        assert(history!!.transactions.size == 2)

        val contractHistoryItem = history.transactions[0]
        assertNotNull(contractHistoryItem)

        val contractOperationHistoryItem = contractHistoryItem.operation
        assertNotNull(contractOperationHistoryItem)
        assert(contractOperationHistoryItem is ContractCreateOperation)
        assert(contractOperationHistoryItem!!.id == OperationType.CONTRACT_CREATE_OPERATION.ordinal.toByte())

        val transferHistoryItem = history.transactions[1]
        assertNotNull(transferHistoryItem)

        val transferOperationHistoryItem = transferHistoryItem.operation
        assertNotNull(transferOperationHistoryItem)
        assert(transferOperationHistoryItem is TransferOperation)
        assert(transferOperationHistoryItem!!.id == OperationType.TRANSFER_OPERATION.ordinal.toByte())
    }

    val result = """{"id": 3,
                                "jsonrpc": "2.0",
                                "result": [
                                            {
                                            "id": "1.6.506",
                                            "op": [
                                                24,
                                                {
                                                  "fee": {
                                                    "amount": 20,
                                                    "asset_id": "1.3.0"
                                                  },
                                                  "registrar": "1.2.22",
                                                  "receiver": "1.16.1",
                                                  "asset_id": "1.3.0",
                                                  "eth_accuracy": false,
                                                  "value": {
                                                    "amount": 20,
                                                    "asset_id": "1.3.0"
                                                  },
                                                  "gasPrice": 0,
                                                  "gas": 1000000,
                                                  "code": "5b34b966"
                                                }
                                              ],
                                            "result": [
                                                1,
                                                "1.17.102"
                                            ],
                                            "block_num": 86614,
                                            "trx_in_block": 0,
                                            "op_in_trx": 0,
                                            "virtual_op": 1437
                                            },
                                            {
                                              "id": "1.6.316",
                                              "op": [
                                                0,
                                                {
                                                  "fee": {
                                                    "amount": 20,
                                                    "asset_id": "1.3.0"
                                                  },
                                                  "from": "1.2.22",
                                                  "to": "1.2.18",
                                                  "amount": {
                                                    "amount": 20000,
                                                    "asset_id": "1.3.24"
                                                  },
                                                  "extensions": []
                                                }
                                              ],
                                              "result": [
                                                0,
                                                {}
                                              ],
                                              "block_num": 65681,
                                              "trx_in_block": 0,
                                              "op_in_trx": 0,
                                              "virtual_op": 900
                                            }
                                            ]}"""
}