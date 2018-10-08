package org.echo.mobile.framework.model.socketoperations

import org.echo.mobile.framework.support.EmptyCallback
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * Test cases for [GetAssetsSocketOperation]
 *
 * @author Daria Pechkovskaya
 */
class GetAssetsSocketOperationTest {

    private lateinit var operation: GetAssetsSocketOperation

    private val testApiId = 3
    private val testCallId = 3
    private val testAssets = arrayOf("1.3.1", "1.3.2")

    @Before
    fun setUp() {
        operation = GetAssetsSocketOperation(
            testApiId,
            testAssets,
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
        Assert.assertEquals(apiName, SocketOperationKeys.ASSETS.key)

        val requestParameters = parameters[2].asJsonArray
        Assert.assertTrue(requestParameters.size() == 1)

        val assets = requestParameters[0].asJsonArray
        Assert.assertTrue(assets.size() == 2)

        val asset = assets[0].asString
        Assert.assertEquals(asset, testAssets[0])
    }

    @Test
    fun deserializeFromJsonTest() {
        val assets = operation.fromJson(result)

        assert(assets.size == 1)

        val asset = assets[0]
        assertNotNull(asset)
        assertEquals(asset.getObjectId(), "1.3.0")
    }

    val result = """{
                      "id": 5,
                      "jsonrpc": "2.0",
                      "result": [
                                {
                                  "id": "1.3.0",
                                  "symbol": "ECHO",
                                  "precision": 5,
                                  "issuer": "1.2.3",
                                  "options": {
                                    "max_supply": "1000000000000000",
                                    "market_fee_percent": 0,
                                    "max_market_fee": "1000000000000000",
                                    "issuer_permissions": 0,
                                    "flags": 0,
                                    "core_exchange_rate": {
                                      "base": {
                                        "amount": 1,
                                        "asset_id": "1.3.0"
                                      },
                                      "quote": {
                                        "amount": 1,
                                        "asset_id": "1.3.0"
                                      }
                                    },
                                    "whitelist_authorities": [],
                                    "blacklist_authorities": [],
                                    "whitelist_markets": [],
                                    "blacklist_markets": [],
                                    "description": "",
                                    "extensions": []
                                  },
                                  "dynamic_asset_data_id": "2.3.0"
                                }
                      ]
                    }"""

}