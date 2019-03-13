package org.echo.mobile.framework.support

import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test

/**
 * Test cases for [EthAddressValidator]
 *
 * @author Dmitriy Bushuev
 */
class EthAddressValidatorTest {

    @Test
    fun isAddressValidTest() {
        val validLowerCaseAddress = "0x3de8c14c8e7a956f5cc4d82beff749ee65fdc358"
        assertTrue(EthAddressValidator.isAddressValid(validLowerCaseAddress))

        val validChecksumAddress = "0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359"
        assertTrue(EthAddressValidator.isAddressValid(validChecksumAddress))

        val invalidLengthAddress = "0x3de8c14c8e7a956f5cc4d82beff749ee65bac35"
        assertFalse(EthAddressValidator.isAddressValid(invalidLengthAddress))

        val invalidChecksumAddress = "0x3de8c14c8E7a956f5cc4d82beff749ee65fdc358"
        assertFalse(EthAddressValidator.isAddressValid(invalidChecksumAddress))
    }

}