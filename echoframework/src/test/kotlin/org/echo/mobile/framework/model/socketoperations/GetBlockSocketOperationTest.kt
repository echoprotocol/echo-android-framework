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
                      "id": 108,
                      "jsonrpc": "2.0",
                      "result": {
                        "previous": "000045648cadccbd88fb91a3d4ee44ed995d48f9",
                        "timestamp": "2018-08-10T14:27:55",
                        "witness": "1.6.10",
                        "transaction_merkle_root": "0c8015ab0d5471216cb0ab3052e0b7b7d115ca3e",
                        "state_root_hash": "8a0250a64258890c789893006acafb51ae544a575457afa77d3e11c2b0847a6a",
                        "result_root_hash": "288912bd68b41a96fefa5d537dbd51411ee066450dc01cd017c01eea977cc491",
                        "extensions": [],
                        "witness_signature": "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                        "ed_signature": "029ee9c3e31eab6d655f416cfa11f8cb70a030444669e6c70e40612ac50ddca0e30862f2e52329a0c014a0191b4d2e45d6d531453bf6c82a5896ec7907d33804",
                        "verifications": [
                          [
                            "1.6.1",
                            "e1c7b4452af5d4c0992adde610561a69f876decc5e04e64b70a2bdfdbc4e4d0aff1ef5e3f426492f4f4dd71ffdf9ae347c25b3e6e37b382adf17a49a3686df00"
                          ]
                        ],
                        "transactions": [
                          {
                            "ref_block_num": 17764,
                            "ref_block_prefix": 3184307596,
                            "expiration": "2018-08-10T14:28:32",
                            "operations": [
                              [
                                6,
                                {
                                  "fee": {
                                    "amount": 2013476,
                                    "asset_id": "1.3.0"
                                  },
                                  "account": "1.2.18",
                                  "owner": {
                                    "weight_threshold": 1,
                                    "account_auths": [],
                                    "key_auths": [
                                      [
                                        "ECHO6r8aCcMXqYbV1hCVh9ny7Xx3eXCqiaR1wjPH1Atra4JyLDL9mK",
                                        1
                                      ]
                                    ],
                                    "address_auths": []
                                  },
                                  "active": {
                                    "weight_threshold": 1,
                                    "account_auths": [],
                                    "key_auths": [
                                      [
                                        "ECHO5xiJsHy6r2m4XBJiRHmpMUqJHrNjzw3aJe6KE5gzwFn1mwKUR9",
                                        1
                                      ]
                                    ],
                                    "address_auths": []
                                  },
                                  "ed_key": "edKey",
                                  "new_options": {
                                    "memo_key": "ECHO5xiJsHy6r2m4XBJiRHmpMUqJHrNjzw3aJe6KE5gzwFn1mwKUR9",
                                    "voting_account": "1.2.5",
                                    "delegating_account": "1.2.12",
                                    "num_witness": 0,
                                    "num_committee": 0,
                                    "votes": [],
                                    "extensions": []
                                  },
                                  "extensions": {}
                                }
                              ]
                            ],
                            "extensions": [],
                            "signatures": [
                              "1f18d73c5f21d390d67786ecaba001664f40248fd1dcc09356619d73f117b2c6484e97e4f5b609b43f1a0c3327a85ab64d8afef54d563b9f300cab828cc70e0f82"
                            ],
                            "operation_results": [
                              [
                                0,
                                {}
                              ]
                            ]
                          }
                        ]
                      }
                    }"""

}