package com.pixelplex.echoframework.model.socketoperations

import com.pixelplex.echoframework.support.EmptyCallback
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test cases for [AccessSocketOperation]
 *
 * @author Dmitriy Bushuev
 */
class AccessSocketOperationTest {

    private lateinit var operation: AccessSocketOperation

    @Before
    fun setUp() {
        operation =
                AccessSocketOperation(
                    AccessSocketOperationType.HISTORY,
                    1,
                    callback = EmptyCallback()
                )
    }

    @Test
    fun serializeTest() {
        operation.callId = 2
        val json = operation.toJsonObject().asJsonObject

        assertEquals(json.get(OperationCodingKeys.ID.key).asInt, 2)
        assertEquals(json.get(OperationCodingKeys.METHOD.key).asString, SocketMethodType.CALL.key)

        val parameters = json.getAsJsonArray(OperationCodingKeys.PARAMS.key)

        assertTrue(parameters.size() == 3)
    }

    @Test
    fun deserializeTest() {
        val id = operation.fromJson(RESULT)

        assertEquals(2, id)
    }

    companion object {
        private const val RESULT = "{\"id\":2,\"jsonrpc\":\"2.0\",\"result\":2}"
    }

}
