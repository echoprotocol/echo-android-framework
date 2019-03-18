package org.echo.mobile.framework.model.operations

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.bitcoinj.ECKey
import org.echo.mobile.framework.model.AccountOptions
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.Authority
import org.echo.mobile.framework.model.BaseOperation
import org.echo.mobile.framework.model.PublicKey
import org.junit.Assert
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Test cases for [AccountCreateOperation]
 *
 * @author Dmitriy Bushuev
 */
class AccountCreateOperationTest {

    @Test
    fun bytesSerializationTest() {
        val operation = buildOperation()

        assertNotNull(operation.toBytes())
    }

    @Test
    fun jsonSerializationTest() {
        val operation = buildOperation()

        val json = operation.toJsonObject().asJsonArray

        Assert.assertTrue(json[0].asByte == OperationType.ACCOUNT_CREATE_OPERATION.ordinal.toByte())

        val transferObject = json[1].asJsonObject

        assertNotNull(transferObject.get(BaseOperation.KEY_FEE))
        assertNotNull(transferObject.get(AccountCreateOperation.KEY_REGISTRAR))
        assertNotNull(transferObject.get(AccountCreateOperation.KEY_REFERRER))
        assertNotNull(transferObject.get(AccountCreateOperation.KEY_REFERRER_PERCENT))
        assertNotNull(transferObject.get(AccountCreateOperation.KEY_ACTIVE))
        assertNotNull(transferObject.get(AccountCreateOperation.KEY_OWNER))
        assertNotNull(transferObject.get(AccountCreateOperation.KEY_OPTIONS))
        assertNotNull(transferObject.get(AccountCreateOperation.KEY_EXTENSIONS))
    }

    @Test
    fun jsonDeserializationTest() {
        // implement
    }

    private fun buildOperation(): AccountCreateOperation {
        val fee = AssetAmount(UnsignedLong.ONE)
        val owner = Authority(2)
        val active = Authority(3)
        val options = AccountOptions(PublicKey(ECKey.fromPublicOnly(ECKey().pubKeyPoint).pubKey))

        return AccountCreateOperationBuilder()
            .setAccountName("testName")
            .setRegistrar("registrar")
            .setReferrer("referrer")
            .setReferrerPercent(5)
            .setOwner(owner)
            .setActive(active)
            .setEdKey("")
            .setOptions(options)
            .setFee(fee)
            .build()
    }

}
