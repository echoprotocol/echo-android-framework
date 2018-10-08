package org.echo.mobile.framework.model.socketoperations

import org.echo.mobile.framework.support.EmptyCallback
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Test cases for [LoginSocketOperation]
 *
 * @author Dmitriy Bushuev
 */
class LoginSocketOperationTest {

    private lateinit var operation: LoginSocketOperation

    @Before
    fun setUp() {
        operation = LoginSocketOperation(2, 3, callback = EmptyCallback())
    }

    @Test
    fun serializeTest() {
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
        Assert.assertEquals(apiName, SocketOperationKeys.LOGIN.key)
    }

    @Test
    fun deserializeTest() {
        val result = operation.fromJson(RESULT)

        Assert.assertTrue(result ?: false)
    }

    companion object {
        private var RESULT = """{"id":3,
                                            |"jsonrpc":"2.0",
                                            |"result":true}
                                        |""".trimMargin()
    }

}
