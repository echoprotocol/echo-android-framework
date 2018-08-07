package com.pixelplex.echoframework.model.contract

import com.pixelplex.bitcoinj.Base58
import com.pixelplex.echoframework.support.Converter
import org.spongycastle.util.encoders.Hex

/**
 * Converts [ContractMethodParameter] to hash string by type
 *
 * @author Daria Pechkovskaya
 */
class ContractCodeConverter : Converter<ContractMethodParameter, String> {

    companion object {
        private const val TYPE_INT = "int"
        private const val TYPE_STRING = "string"
        private const val TYPE_ADDRESS = "address"
        private const val HASH_PATTERN =
            "0000000000000000000000000000000000000000000000000000000000000000"
        private const val RADIX = 16
    }

    private var mParamsCount: Long = 0
    private var currStringOffset: Long = 0

    override fun convert(source: ContractMethodParameter): String {
        val value = source.value

        return when {
            source.type.contains(TYPE_INT) -> appendNumericPattern(convertToByteCode(value.toLong()))
            source.type.contains(TYPE_STRING) -> getStringOffset(value)
            source.type.contains(TYPE_ADDRESS) -> appendAddressPattern(
                Hex.toHexString(Base58.decode(value)).substring(2, 42)
            )
            else -> ""
        }
    }

    private fun appendAddressPattern(value: String): String {
        return HASH_PATTERN.substring(value.length) + value
    }

    private fun formNotFullString(value: String): String {
        return value + HASH_PATTERN.substring(value.length)
    }

    private fun appendNumericPattern(value: String): String {
        return HASH_PATTERN.substring(0, HASH_PATTERN.length - value.length) + value
    }

    private fun convertToByteCode(value: Long): String {
        return value.toString(RADIX)
    }

    private fun getStringOffset(value: String): String {
        val currOffset = (mParamsCount + currStringOffset) * 32
        currStringOffset =
                (getStringHash(value).length / HASH_PATTERN.length + 1).toLong()
        return appendNumericPattern(convertToByteCode(currOffset))
    }

    private fun getStringHash(value: String): String {
        return if (value.length <= HASH_PATTERN.length) {
            formNotFullString(value)
        } else {
            val ost = value.length % HASH_PATTERN.length
            value + HASH_PATTERN.substring(0, HASH_PATTERN.length - ost)
        }
    }
}
