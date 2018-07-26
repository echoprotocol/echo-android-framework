package com.pixelplex.echoframework.model.socketoperations

import com.google.common.primitives.UnsignedLong
import com.google.gson.GsonBuilder
import com.pixelplex.echoframework.model.*
import com.pixelplex.echoframework.model.network.Testnet
import com.pixelplex.echoframework.model.operations.OperationType
import com.pixelplex.echoframework.model.operations.TransferOperation
import com.pixelplex.echoframework.model.operations.TransferOperationBuilder
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test cases for [TransferOperation]
 *
 * @author Dmitriy Bushuev
 */
class TransferOperationTest {

    @Test
    fun bytesSerializationTest() {
        val operation = buildOperation()

        Assert.assertNotNull(operation.toBytes())
    }

    @Test
    fun jsonSerializationTest() {
        val operation = buildOperation()

        val json = operation.toJsonObject().asJsonArray

        assertTrue(json[0].asByte == OperationType.TRANSFER_OPERATION.ordinal.toByte())

        val transferObject = json[1].asJsonObject

        Assert.assertNotNull(transferObject.get(TransferOperation.KEY_FEE))
        Assert.assertNotNull(transferObject.get(TransferOperation.KEY_FROM))
        Assert.assertNotNull(transferObject.get(TransferOperation.KEY_TO))
        Assert.assertNotNull(transferObject.get(TransferOperation.KEY_AMOUNT))
        Assert.assertNotNull(transferObject.get(TransferOperation.KEY_EXTENSIONS))
    }

    @Test
    fun jsonDeserializationTest() {
        val json = """|{
                                |"fee": {
                                    |"amount": 264174,
                                   | "asset_id": "1.3.0"
                                |},
                                |"from": "1.2.23215",
                                |"to": "1.2.23216",
                                |"amount": {
                                    |"amount": 100,
                                    |"asset_id": "1.3.0"
                                |},
                                |"extensions": []
                       |}
                    """.trimMargin()

        val gson = configureGson()

        val transfer = gson.fromJson<TransferOperation>(json, TransferOperation::class.java)

        assertTrue(transfer.type == OperationType.TRANSFER_OPERATION)
        assertTrue(transfer.fee.amount == UnsignedLong.valueOf(264174))
        assertTrue(transfer.from?.getObjectId() == "1.2.23215")
        assertTrue(transfer.to?.getObjectId() == "1.2.23216")
    }

    private fun configureGson() = GsonBuilder().apply {
        registerTypeAdapter(
            TransferOperation::class.java,
            TransferOperation.TransferDeserializer()
        )
        registerTypeAdapter(AssetAmount::class.java, AssetAmount.Deserializer())
        registerTypeAdapter(Authority::class.java, Authority.Deserializer(Testnet()))
        registerTypeAdapter(Account::class.java, Account.Deserializer())
        registerTypeAdapter(AccountOptions::class.java, AccountOptions.Deserializer(Testnet()))
    }.create()

    private fun buildOperation(): TransferOperation {
        val fee = AssetAmount(UnsignedLong.ONE)
        val fromAccount = Account("1.2.23215")
        val toAccount = Account("1.2.23216")
        val amount = AssetAmount(UnsignedLong.valueOf(10000), Asset("1.3.0"))

        return TransferOperationBuilder()
            .setFrom(fromAccount)
            .setTo(toAccount)
            .setFee(fee)
            .setAmount(amount)
            .build()
    }

}