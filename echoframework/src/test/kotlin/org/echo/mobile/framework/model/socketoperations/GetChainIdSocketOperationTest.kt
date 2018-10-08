package org.echo.mobile.framework.model.socketoperations

import org.echo.mobile.framework.support.EmptyCallback
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Test cases for [GetChainIdSocketOperation]
 *
 * @author Dmitriy Bushuev
 */
class GetChainIdSocketOperationTest {

    private lateinit var operation: GetChainIdSocketOperation

    @Before
    fun setUp() {
        operation = GetChainIdSocketOperation(
            2,
            3,
            callback = EmptyCallback()
        )
    }

    @Test
    fun serializeTest() {
        operation = GetChainIdSocketOperation(
            2,
            3,
            callback = EmptyCallback()
        )
        val json = operation.toJsonObject().asJsonObject

        Assert.assertEquals(json.get(OperationCodingKeys.ID.key).asInt, 3)
        Assert.assertEquals(
            json.get(OperationCodingKeys.METHOD.key).asString,
            SocketMethodType.CALL.key
        )

        val parameters = json.getAsJsonArray(OperationCodingKeys.PARAMS.key)
        Assert.assertTrue(parameters.size() == 3)

        val apiId = parameters[0].asInt
        Assert.assertEquals(apiId, 2)

        val apiName = parameters[1].asString
        Assert.assertEquals(apiName, SocketOperationKeys.CHAIN_ID.key)
    }

    @Test
    fun deserializeTest() {
        val chainId = operation.fromJson(RESULT)

        Assert.assertEquals(CHAIN_ID, chainId)
    }

    companion object {
        private const val CHAIN_ID =
            "39f5e2ede1f8bc1a3a54a7914414e3779e33193f1f5693510e73cb7a87617447"

        private var RESULT = """{"id":3,
                                            |"jsonrpc":"2.0",
                                            |"result":"$CHAIN_ID"}
                                        |""".trimMargin()
    }

}
