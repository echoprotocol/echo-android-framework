package com.pixelplex.echolib.support.operationbuilders

import com.google.common.primitives.UnsignedLong
import com.pixelplex.bitcoinj.ECKey
import com.pixelplex.echolib.exception.MalformedOperationException
import com.pixelplex.echolib.model.*
import com.pixelplex.echolib.model.operations.AccountUpdateOperation
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
        val owner = Authority(2)
        val active = Authority(3)
        val options = AccountOptions(PublicKey(ECKey.fromPublicOnly(ECKey().pubKeyPoint)))

        return AccountUpdateOperationBuilder()
            .setFee(fee)
            .setAccount(account)
            .setOwner(owner)
            .setActive(active)
            .setOptions(options)
            .build()
    }

}
