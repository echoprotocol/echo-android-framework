package org.echo.mobile.framework.support.operationbuilders

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.bitcoinj.ECKey
import org.echo.mobile.framework.exception.MalformedOperationException
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AccountOptions
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.Authority
import org.echo.mobile.framework.model.PublicKey
import org.echo.mobile.framework.model.operations.AccountUpdateOperation
import org.echo.mobile.framework.model.operations.AccountUpdateOperationBuilder
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test cases for [AccountUpdateOperationBuilder]
 *
 * @author Dmitriy Bushuev
 */
class AccountUpdateOperationBuilderTest {

    @Test(expected = MalformedOperationException::class)
    fun emptyBuildTest() {
        AccountUpdateOperationBuilder().build()
    }

    @Test
    fun successBuildTest() {
        val operation = buildOperation()

        assertEquals(operation.fee.amount, UnsignedLong.ONE)
    }

    private fun buildOperation(): AccountUpdateOperation {
        val fee = AssetAmount(UnsignedLong.ONE)
        val account = Account("1.2.23215")
        val active = Authority(2)
        val options = AccountOptions(PublicKey(ECKey.fromPublicOnly(ECKey().pubKeyPoint).pubKey))

        return AccountUpdateOperationBuilder()
            .setFee(fee)
            .setAccount(account)
            .setActive(active)
            .setEdKey("")
            .setOptions(options)
            .build()
    }

}
