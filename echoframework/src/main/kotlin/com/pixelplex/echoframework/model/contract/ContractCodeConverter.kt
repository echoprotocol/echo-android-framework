package com.pixelplex.echoframework.model.contract

import com.pixelplex.bitcoinj.Base58
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.support.Converter
import org.spongycastle.util.encoders.Hex
import java.math.BigDecimal
import java.math.BigInteger
import java.util.regex.Pattern

/**
 * Converts [ContractMethodParameter] to hash string by type
 *
 * @author Daria Pechkovskaya
 */
class ContractCodeConverter : Converter<ContractMethodParameter, String> {

    companion object {
        private const val HASH_PATTERN =
            "0000000000000000000000000000000000000000000000000000000000000000" //64b

        private const val ARRAY_PARAMETER_CHECK_PATTERN = ".*?\\d+\\[\\d*]"
        private const val ARRAY_PARAMETER_TYPE = "(.*?\\d+)\\[(\\d*)]"

        private const val RADIX = 16
    }

    private var mParamsCount: Long = 0
    private var currStringOffset: Long = 0

    override fun convert(source: ContractMethodParameter): String {
        val value = source.value

        return when {
            source.type.contains(ContractMethodParameter.TYPE_STRING) || parameterIsArray(source) ->
                getStringOffset(value)

            source.type.contains(ContractMethodParameter.TYPE_BOOL) ->
                appendBooleanPattern(value)

            source.type.contains(ContractMethodParameter.TYPE_INT) ->
                appendNumericPattern(convertToByteCode(BigDecimal(value).toBigInteger()))

            source.type.contains(ContractMethodParameter.TYPE_ADDRESS) ->
                appendAddressPattern(Hex.toHexString(Base58.decode(value)).substring(2, 42))

            else -> ""
        }
    }

    private fun parameterIsArray(contractMethodParameter: ContractMethodParameter): Boolean {
        val p = Pattern.compile(ARRAY_PARAMETER_CHECK_PATTERN)
        val m = p.matcher(contractMethodParameter.type)
        return m.matches()
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

    private fun convertToByteCode(value: BigInteger): String {
        return value.toString(RADIX)
    }

    private fun convertToByteCode(value: Long): String {
        return value.toString(RADIX)
    }

    private fun appendBooleanPattern(parameter: String): String {
        return if (java.lang.Boolean.valueOf(parameter)) {
            appendNumericPattern("1")
        } else {
            appendNumericPattern("0")
        }
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

    private fun appendArrayParameters(parameter: ContractMethodParameter): String {
        var stringParams = ""

        if (parameterIsArray(parameter)) {
            val arrayTypeAndLength = getArrayTypeAndLength(parameter)
            val paramsList = getArrayValues(parameter)
            if (paramsList.isNotEmpty()) {
                val arrayLength = if (arrayTypeAndLength.second.isEmpty()) {
                    paramsList.size.toString()
                } else {
                    arrayTypeAndLength.second
                }
                stringParams += appendNumericPattern(arrayLength)
                for (item in paramsList) {
                    stringParams += appendArrayParameter(arrayTypeAndLength.first, item)
                }
            }
        }

        return stringParams
    }

    private fun getArrayTypeAndLength(contractMethodParameter: ContractMethodParameter): Pair<String, String> {
        val p = Pattern.compile(ARRAY_PARAMETER_TYPE)
        val m = p.matcher(contractMethodParameter.type)
        return if (m.matches()) {
            m.group(1) to (m.group(2) ?: "")
        } else {
            throw LocalException("Can not find type for ABI array parameter: ${contractMethodParameter.type}")
        }
    }

    private fun getArrayValues(contractMethodParameter: ContractMethodParameter): List<String> {
        return contractMethodParameter.value.split("\\s*,\\s*".toRegex())
            .dropLastWhile { it.isEmpty() }
    }

    private fun appendArrayParameter(type: String, param: String): String {
        return when {
            type.contains(ContractMethodParameter.TYPE_INT) ->
                appendNumericPattern(convertToByteCode(BigInteger(param)))

            type.contains(ContractMethodParameter.TYPE_BOOL) ->
                appendBooleanPattern(param)

            type.contains(ContractMethodParameter.TYPE_ADDRESS) ->
                appendAddressPattern(param)

            else -> ""
        }
    }
}
