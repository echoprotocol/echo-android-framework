package org.echo.mobile.framework.support

import org.echo.mobile.framework.support.crypto.decryptAES
import org.echo.mobile.framework.support.crypto.encryptAES
import org.junit.Assert
import org.junit.Test
import org.spongycastle.crypto.InvalidCipherTextException

/**
 * Test cases for support cryptographic utility methods
 *
 * @author Dmitriy Bushuev
 */
class CryptographicUtilsTest {

    private val shortMessage = "short"
    private val longMessage = "longlonglonglonglonglonglonglonglonglonglonglonglonglon" +
            "longlonglonglonglonglonglonglonglonglonglonglonglonglonglonglonglonglonglonglong" +
            "longlonglonglonglonglonglonglonglonglonglonglonglonglonglonglonglonglonglonglong" +
            "longlonglonglonglonglonglonglonglonglonglonglonglonglonglonglonglonglonglonglong" +
            "longlonglonglonglonglonglonglonglonglonglonglonglonglonglonglonglonglonglonglong"

    private val seed = "seed".toByteArray()

    @Test
    fun encryptShortAESTest() {
        val encrypted =
            encryptAES(shortMessage.toByteArray(), seed)

        Assert.assertNotNull(encrypted)
    }

    @Test
    fun encryptLongAESTest() {
        val encrypted =
            encryptAES(longMessage.toByteArray(), seed)

        Assert.assertNotNull(encrypted)
    }

    @Test
    fun decryptShortTest() {
        val encrypted =
            encryptAES(shortMessage.toByteArray(), seed)

        val decrypted = decryptAES(encrypted!!, seed)

        Assert.assertEquals(String(decrypted!!), shortMessage)
    }

    @Test
    fun decryptLongTest() {
        val encrypted =
            encryptAES(longMessage.toByteArray(), seed)

        val decrypted = decryptAES(encrypted!!, seed)

        Assert.assertEquals(String(decrypted!!), longMessage)
    }

    @Test(expected = InvalidCipherTextException::class)
    fun decryptErrorTest() {
        val encrypted = encryptAES(
            longMessage.toByteArray(),
            "wrongSeed".toByteArray()
        )

        decryptAES(encrypted!!, seed)
    }

}
