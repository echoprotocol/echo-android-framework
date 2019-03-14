package org.echo.mobile.framework.support

import org.spongycastle.jcajce.provider.digest.Keccak
import org.spongycastle.util.encoders.Hex
import java.util.regex.Pattern

/**
 * Utility class for validating ethereum addresses
 *
 * @author Dmitriy Bushuev
 */
object EthAddressValidator {

    private val ignoreCaseAddrPattern = Pattern.compile("(?i)^(0x)?[0-9a-f]{40}$")
    private val lowerCaseAddrPattern = Pattern.compile("^(0x)?[0-9a-f]{40}$")
    private val upperCaseAddrPattern = Pattern.compile("^(0x)?[0-9A-F]{40}$")

    const val ADDRESS_PREFIX = "0x"

    /**
     * Enter point of address validation process
     */
    fun isAddressValid(ethAddress: String): Boolean {
        // check basic address requirements, i.e. is not empty and contains
        // the valid number and type of characters
        if (ethAddress.isEmpty() || !ignoreCaseAddrPattern.matcher(ethAddress).find()) return false

        val sameCaps = isAllSameCaps(ethAddress)

        if (!sameCaps) {
            // if it is mixed caps it is a checksum address and needs to be validated
            return isValidChecksum(ethAddress)
        }

        // if it's all small caps or caps return true
        return sameCaps
    }

    private fun isAllSameCaps(ethAddress: String): Boolean {
        return lowerCaseAddrPattern.matcher(ethAddress).find()
                || upperCaseAddrPattern.matcher(ethAddress).find()
    }

    private fun isValidChecksum(ethAddress: String): Boolean {
        val address = ethAddress.replace(ADDRESS_PREFIX, "")
        val lowerCased = address.toLowerCase()

        val hash = hash(lowerCased)

        for (i in 0..39) {
            if (Character.isLetter(address[i])) {
                // each uppercase letter should correlate with a first bit of 1 in the hash
                // char with the same index, and each lowercase letter with a 0 bit
                val charInt = Integer.parseInt(Character.toString(hash[i]), 16)
                if (isValidUppercase(address[i], charInt) ||
                    isValidLowercase(address[i], charInt)
                ) {
                    return false
                }
            }
        }

        return true
    }

    private fun hash(address: String): String {
        val keccak = Keccak.Digest256().apply {
            update(address.toByteArray(Charsets.UTF_8))
        }
        val sha3bytes = keccak.digest()

        return Hex.toHexString(sha3bytes)
    }

    private fun isValidUppercase(symbol: Char, charInt: Int) =
        Character.isUpperCase(symbol) && charInt <= 7

    private fun isValidLowercase(symbol: Char, charInt: Int) =
        Character.isLowerCase(symbol) && charInt > 7

}