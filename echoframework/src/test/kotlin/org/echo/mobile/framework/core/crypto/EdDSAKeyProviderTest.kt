package org.echo.mobile.framework.core.crypto

import org.echo.mobile.framework.core.crypto.internal.eddsa.EdDSASecurityProvider
import org.echo.mobile.framework.core.crypto.internal.eddsa.key.IrohaKeyPairCryptoAdapter
import org.echo.mobile.framework.core.crypto.internal.eddsa.key.KeyPairCryptoAdapter
import org.echo.mobile.framework.core.crypto.internal.eddsa.key.NaCLKeyPairCryptoAdapter
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.spongycastle.util.encoders.Hex
import java.security.Security

/**
 * Test cases for [KeyPairCryptoAdapter] implementations
 *
 * @author Dmitriy Bushuev
 */
class EdDSAKeyProviderTest {

    private val irohaAdapter by lazy { IrohaKeyPairCryptoAdapter() }
    private val naclAdapter by lazy { NaCLKeyPairCryptoAdapter() }

    @Before
    fun setup() {
        Security.addProvider(EdDSASecurityProvider())
    }

    @Test
    fun getIrohaKeyBySeedTest() {
        val echorandKey = Hex.toHexString(irohaAdapter.keyPair(Hex.decode(PRIVATE_KEY_MOCK)).first)

        assertEquals(
            echorandKey,
            "a08fd46ee534e62d08e577a84a28601903d424bdf288be45644ece293672943e"
        )
    }

    @Test
    fun getNaClKeyBySeedTest() {
        val echorandKey = Hex.toHexString(naclAdapter.keyPair(Hex.decode(PRIVATE_KEY_MOCK)).first)

        assertEquals(
            echorandKey,
            "3ec09a863f81f9adc6ef1c865295fada64550430b4b31aa40ef6c9b593290b5f"
        )
    }

    companion object {
        private const val PRIVATE_KEY_MOCK =
            "c5aa8df43f9f837bedb7442f31dcb7b166d38535076f094b85ce3a2e0b4458f7"
    }

}