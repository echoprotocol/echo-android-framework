package org.echo.mobile.framework.core.crypto

import org.echo.mobile.framework.core.crypto.internal.eddsa.EdDSASecurityProvider
import org.echo.mobile.framework.core.crypto.internal.eddsa.signature.EdDSAIrohaSignatureAdapter
import org.echo.mobile.framework.core.crypto.internal.eddsa.signature.SignatureAdapter
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.spongycastle.util.encoders.Hex
import java.security.Security

/**
 * Test cases for [SignatureAdapter] implementations
 *
 * @author Dmitriy Bushuev
 */
class EdDSASignatureTest {

    @Before
    fun setup() {
        Security.addProvider(EdDSASecurityProvider())
    }

    @Test
    fun signatureTest() {
        val source = Hex.decode("746573746d7367")

        val signatureAdapter = EdDSAIrohaSignatureAdapter()
        val signature = signatureAdapter.sign(source, Hex.decode(PRIVATE_KEY_MOCK))

        Assert.assertEquals(
            Hex.toHexString(signature),
            "f457ae1fd4f4ff52ea09f807bdda0eddfeb0" +
                    "5467c2c24df1009b9d63ce6dab4fd391395" +
                    "be4d41540f582d937c4accea360d2be13ff7e17a084d1016aeb56f308"
        )
    }

    companion object {
        private const val PRIVATE_KEY_MOCK =
            "c5aa8df43f9f837bedb7442f31dcb7b166d38535076f094b85ce3a2e0b4458f7"
    }

}