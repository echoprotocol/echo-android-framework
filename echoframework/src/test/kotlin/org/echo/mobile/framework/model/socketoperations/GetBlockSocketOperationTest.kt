package org.echo.mobile.framework.model.socketoperations

import org.echo.mobile.framework.model.network.Testnet
import org.echo.mobile.framework.support.EmptyCallback
import org.junit.Assert
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * Test cases for [GetBlockSocketOperation]
 *
 * @author Daria Pechkovskaya
 */
class GetBlockSocketOperationTest {

    private lateinit var operation: GetBlockSocketOperation

    private val testApiId = 3
    private val testCallId = 3
    private val testBlockNumber = "123456"

    @Before
    fun setUp() {
        operation = GetBlockSocketOperation(
            testApiId,
            testBlockNumber,
            testCallId,
            callback = EmptyCallback(),
            network = Testnet()
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
        Assert.assertEquals(apiName, SocketOperationKeys.BLOCK.key)

        val requestParameters = parameters[2].asJsonArray
        Assert.assertTrue(requestParameters.size() == 1)

        val blockNumber = requestParameters[0].asString
        Assert.assertEquals(blockNumber, testBlockNumber)
    }

    @Test
    fun deserializeFromJsonTest() {
        val block = operation.fromJson(result)

        assertNotNull(block)
        assertNotNull(block!!.transactions)
        assert(block.transactions.size == 1)
    }

    val result = """{
                      "id": 6,
                      "jsonrpc": "2.0",
                      "result": {
                        "previous": "00028b0289fea4607aaf9564031eb10bec13a549",
                        "timestamp": "2019-04-24T08:25:12",
                        "account": "1.2.12",
                        "transaction_merkle_root": "34e64b73ac1fd5e146e4033aed1ea3011d99152f",
                        "vm_root": ["e3054d080d1dce5eb5905b7641806399ad9dfa4627a7f3f811fa48459b3035cc.5f15d8519e6ea1d967b645eeb0b73269f93b0f8b9bca71ac94f320e629db09d3 0.9a71ff66a2f503e4e96c4e9a2521d6a710f2d373b422332029da170d78fa1a68"],
                        "round": 166659,
                        "extensions": [],
                        "ed_signature": "18f5c5b77248d71f33f521f06f32e077b1c203a601cef0241724dc6c9277c0ddd6510ca0229179c0bc934fd8541fc16b57b9a8abf51e0773776f3bb13e0d880e",
                        "rand": "9ac4c73efeead66584877038963918c27477b831f4f5f302dc322fa63ea13337",
                        "prev_signatures":  [
                            {
                              "_step": 4,
                              "_value": 0,
                              "_signer": 11,
                              "_bba_sign": "c2aaadb03d637f0bfb514b7fd6fa3a6d3b65dd867dd3118163de1a46c0f05dcee63bd1bd6bfc41f3b688570cf84ccd93a59ec17a77e731ad5cccf7b9fa2ec106"
                            },
                            {
                              "_step": 4,
                              "_value": 0,
                              "_signer": 12,
                              "_bba_sign": "431a882c9a4e74e3b11972c2d5c3888bba3017452473c1d925fc21d68b4aba82b0b29f981c660ea70685452f623f4222bbddd0e21081a5aa8c6aa588d6287808"
                            },
                            {
                              "_step": 4,
                              "_value": 0,
                              "_signer": 6,
                              "_bba_sign": "d1aab4380a5704468780b8e6ab96ab28c14bd996990ea3010c776bed2130994593ff91f1b08002bec239a00d5ed9452a36a26fa93b4ae463333dc3e09f0a3c0f"
                            },
                            {
                              "_step": 4,
                              "_value": 0,
                              "_signer": 41,
                              "_bba_sign": "431a882c9a4e74e3b11972c2d5c3888bba3017452473c1d925fc21d68b4aba82b0b29f981c660ea70685452f623f4222bbddd0e21081a5aa8c6aa588d6287808"
                            },
                            {
                              "_step": 4,
                              "_value": 0,
                              "_signer": 15,
                              "_bba_sign": "431a882c9a4e74e3b11972c2d5c3888bba3017452473c1d925fc21d68b4aba82b0b29f981c660ea70685452f623f4222bbddd0e21081a5aa8c6aa588d6287808"
                            }
                          ],
                        "transactions": [
                          {
                            "ref_block_num": 35585,
                            "ref_block_prefix": 1513105665,
                            "expiration": "2019-04-24T08:25:55",
                            "operations": [
                              [
                                41,
                                {
                                  "fee": {
                                    "amount": 124,
                                    "asset_id": "1.3.0"
                                  },
                                  "registrar": "1.2.16",
                                  "value": {
                                    "amount": 0,
                                    "asset_id": "1.3.0"
                                  },
                                  "code": "1361c394",
                                  "callee": "1.14.0"
                                }
                              ]
                            ],
                            "extensions": [],
                            "signatures": [
                              "5d5390479b0b90c7850ce9157f5dc9c0039acca9c3606fb54c2fcbf7555d1ffd2815b08e426e2bb6f81e9fac4d0da9cdde6ef6b0436d0469c79f46b64475e100"
                            ],
                            "operation_results": [
                              [
                                1,
                                "1.15.8"
                              ]
                            ]
                          }
                        ]
                      }
                    }"""

}