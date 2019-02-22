package org.echo.mobile.framework.core.crypto

import org.echo.mobile.framework.core.crypto.internal.DefaultWifProcessor
import org.junit.Assert
import org.junit.Test
import org.spongycastle.util.encoders.Hex

/**
 * Test cases for [DefaultWifProcessor]
 *
 * @author Dmitriy Bushuev
 */
class DefaultWifProcessorTest {

    private val processor by lazy { DefaultWifProcessor(true) }

    @Test
    fun encodeTest() {
        val privateKey = "0c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d"

        val encoded = processor.encodeToWif(Hex.decode(privateKey))

        Assert.assertEquals(encoded, "5HueCGU8rMjxEXxiPuD5BDku4MkFqeZyd4dZ1jvhTVqvbTLvyTJ")
    }

    @Test
    fun decodeTest() {
        val privateKey = "0c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d"
        val wif = "5HueCGU8rMjxEXxiPuD5BDku4MkFqeZyd4dZ1jvhTVqvbTLvyTJ"

        val decoded = Hex.toHexString(processor.decodeFromWif(wif))

        Assert.assertEquals(privateKey, decoded)
    }
}