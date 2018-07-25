package com.pixelplex.echoframework.model.socketoperations

import com.google.common.primitives.UnsignedLong
import com.pixelplex.bitcoinj.ECKey
import com.pixelplex.echoframework.model.*
import com.pixelplex.echoframework.model.operations.AccountUpdateOperation
import com.pixelplex.echoframework.model.operations.OperationType
import com.pixelplex.echoframework.support.operationbuilders.AccountUpdateOperationBuilder
import org.junit.Assert
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Test cases for [AccountUpdateOperation]
 *
 * @author Dmitriy Bushuev
 */
class AccountUpdateOperationTest {

    @Test
    fun bytesSerializationTest() {
        val operation = buildOperation()

        assertNotNull(operation.toBytes())
    }

    @Test
    fun jsonSerializationTest() {
        val operation = buildOperation()

        val json = operation.toJsonObject().asJsonArray

        Assert.assertTrue(json[0].asByte == OperationType.ACCOUNT_UPDATE_OPERATION.ordinal.toByte())

        val accountUpdateObject = json[1].asJsonObject

        assertNotNull(accountUpdateObject.get(AccountUpdateOperation.KEY_FEE))
        assertNotNull(accountUpdateObject.get(AccountUpdateOperation.KEY_OWNER))
        assertNotNull(accountUpdateObject.get(AccountUpdateOperation.KEY_ACTIVE))
        assertNotNull(accountUpdateObject.get(AccountUpdateOperation.KEY_ACCOUNT))
        assertNotNull(accountUpdateObject.get(AccountUpdateOperation.KEY_NEW_OPTIONS))
        assertNotNull(accountUpdateObject.get(AccountUpdateOperation.KEY_EXTENSIONS))
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

}
