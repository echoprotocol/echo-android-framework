package com.pixelplex.echoframework.model.socketoperations

import com.google.common.primitives.UnsignedLong
import com.pixelplex.bitcoinj.ECKey
import com.pixelplex.echoframework.model.*
import com.pixelplex.echoframework.model.operations.AccountUpdateOperation
import com.pixelplex.echoframework.model.operations.AccountUpdateOperationBuilder
import com.pixelplex.echoframework.support.EmptyCallback
import com.pixelplex.echoframework.support.toUnsignedLong
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Test cases for [RequiredFeesSocketOperation]
 *
 * @author Dmitriy Bushuev
 */
class RequiredFeesSocketOperationTest {

    private lateinit var operation: RequiredFeesSocketOperation
    private lateinit var updateOperation: AccountUpdateOperation

    @Before
    fun setUp() {
        updateOperation = buildOperation()
        operation = RequiredFeesSocketOperation(
            2, listOf(updateOperation), Asset("1.3.0"), 3,
            callback = EmptyCallback()
        )
    }

    private fun buildOperation(): AccountUpdateOperation {
        val fee = AssetAmount(UnsignedLong.ONE)
        val account = Account("1.2.23215")
        val owner = Authority(2)
        val active = Authority(3)
        val options = AccountOptions(PublicKey(ECKey.fromPublicOnly(ECKey().pubKeyPoint).pubKey))

        return AccountUpdateOperationBuilder()
            .setFee(fee)
            .setAccount(account)
            .setOwner(owner)
            .setActive(active)
            .setOptions(options)
            .build()
    }

    @Test
    fun serializeTest() {
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
        assertEquals(apiName, SocketOperationKeys.REQUIRED_FEES.key)
    }

    @Test
    fun deserializeTest() {
        val result = operation.fromJson(RESULT)

        assertNotNull(result)
        assertTrue(result!!.size == 2)
        assertEquals(result[0].amount, 100L.toUnsignedLong())
        assertEquals(result[0].asset.getObjectId(), "1.3.0")
        assertEquals(result[1].amount, 200L.toUnsignedLong())
        assertEquals(result[1].asset.getObjectId(), "1.3.1")
    }

    companion object {
        private var RESULT = """{"id":6,
                                            |"jsonrpc":"2.0",
                                            |"result":
                                            |[
                                                |{
                                                    |"amount":100,
                                                    |"asset_id":"1.3.0"
                                                |},
                                                |{
                                                    |"amount":200,
                                                    |"asset_id":"1.3.1"
                                                |}
                                            |]}
                                        |""".trimMargin()
    }

}