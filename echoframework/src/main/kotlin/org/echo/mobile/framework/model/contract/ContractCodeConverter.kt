package org.echo.mobile.framework.model.contract

import org.echo.mobile.bitcoinj.Base58
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.support.Converter
import org.spongycastle.util.encoders.Hex
import java.math.BigDecimal
import java.math.BigInteger
import java.util.regex.Pattern

/**
 * Converts [ContractMethodParameter] to hash string by type
 *
 * @author Daria Pechkovskaya
 */
class ContractCodeConverter(private var paramsCount: Int) :
    Converter<ContractMethodParameter, String> {

    companion object {
        private const val HASH_PATTERN =
            "0000000000000000000000000000000000000000000000000000000000000000" //64b

        private const val ARRAY_PARAMETER_CHECK_PATTERN = ".*?\\d+\\[\\d*]"
        private const val ARRAY_PARAMETER_TYPE = "(.*?\\d+)\\[(\\d*)]"

        private const val RADIX = 16
    }

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

            source.type.contains(ContractMethodParameter.TYPE_ADDRESS) && value.length == 34 ->
                appendAddressPattern(Hex.toHexString(Base58.decode(value)).substring(2, 42))

            source.type.contains(ContractMethodParameter.TYPE_ADDRESS) ->
                appendNumericPattern(convertToByteCode(BigDecimal(value).toBigInteger()))

            else -> ""
        }
    }

    /**
     * Checks whether parameter of contract call is array
     */
    fun parameterIsArray(contractMethodParameter: ContractMethodParameter): Boolean {
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
        val currOffset = (paramsCount + currStringOffset) * 32
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

    /**
     * Appends string pattern to contract call parameters
     */
    fun appendStringPattern(_value: String): String {
        val value = convertToByteCode(_value)

        var fullParameter = ""
        fullParameter += getStringLength(value)

        fullParameter += if (value.length <= HASH_PATTERN.length) {
            formNotFullString(value)
        } else {
            val ost = value.length % HASH_PATTERN.length
            value + HASH_PATTERN.substring(0, HASH_PATTERN.length - ost)
        }

        return fullParameter
    }

    private fun convertToByteCode(_value: String): String {
        return Hex.toHexString(_value.toByteArray(Charsets.UTF_8))
    }

    private fun getStringLength(_value: String): String {
        return appendNumericPattern(convertToByteCode(String(Hex.decode(_value)).length.toLong() * 2))
    }

    /**
     * Appends array parameter pattern to contract call parameters
     */
    fun appendArrayParameter(parameter: ContractMethodParameter): String {
        var stringParams = ""

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
