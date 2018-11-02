package org.echo.mobile.framework.model.contract.input

import org.echo.mobile.framework.exception.LocalException
import org.spongycastle.util.encoders.Hex
import java.math.BigDecimal
import java.math.BigInteger
import java.util.regex.Pattern

private const val HASH_PATTERN =
    "0000000000000000000000000000000000000000000000000000000000000000" //64b

private const val RADIX = 16

/**
 * Encoding parameter default slice size
 */
const val INPUT_SLICE_SIZE = 32

/**
 * Describes base functionality of contract input parameter model type
 *
 * @author Dmitriy Bushuev
 */
interface InputValueType {

    /**
     * Current encoding context
     */
    var encodingContext: EncodingContext?

    /**
     * Type name of parameter
     */
    var name: String

    /**
     * Encodes [source] according to required type
     */
    fun encode(source: String): String

}

/**
 * Describes context of current encoding process
 */
interface EncodingContext {

    /**
     * Current offset of dynamic contract parameters
     */
    var dynamicParametersOffset: Long

    /**
     * Params count of contract method
     */
    val paramsCount: Int

    /**
     * Appends dynamic parameter data part to the encoding context
     */
    fun appendDynamicDataPart(value: String)

}

/**
 * Extension of [InputValueType] for boolean type
 *
 * 1 - true
 * else - false
 */
class BooleanInputValueType : InputValueType {

    override var encodingContext: EncodingContext? = null

    override var name: String = "bool"

    override fun encode(source: String): String {
        return if (java.lang.Boolean.valueOf(source)) {
            appendNumericPattern("1")
        } else {
            appendNumericPattern("0")
        }
    }

}

/**
 * Implementation of [InputValueType] for number types
 */
class NumberInputValueType(override var name: String) : InputValueType {

    override var encodingContext: EncodingContext? = null

    override fun encode(source: String): String =
        appendNumericPattern(convertToByteCode(BigDecimal(source).toBigInteger()))

}

/**
 * Implementation of [InputValueType] for any address types
 */
class AddressInputValueType : InputValueType {

    override var encodingContext: EncodingContext? = null

    override var name: String = "address"

    override fun encode(source: String): String = when {

        source.startsWith(AccountAddressInputValueType.PREFIX) -> {
            AccountAddressInputValueType().encode(source)
        }
        source.startsWith(ContractAddressInputValueType.PREFIX) -> {
            ContractAddressInputValueType().encode(source)
        }
        else -> {
            throw LocalException("Unable to determine address type for value: $source")
        }
    }

}

/**
 * Implementation of [InputValueType] for account address types
 */
class AccountAddressInputValueType : InputValueType {

    override var encodingContext: EncodingContext? = null

    override var name: String = "address"

    companion object {
        const val PREFIX = "1.2."
    }

    override fun encode(source: String): String {
        val encodeSource = if (source.startsWith(PREFIX)) source.removePrefix(PREFIX) else source
        return appendNumericPattern(convertToByteCode(BigDecimal(encodeSource).toBigInteger()))
    }
}

/**
 * Implementation of [InputValueType] for contract address types
 */
class ContractAddressInputValueType : InputValueType {

    override var encodingContext: EncodingContext? = null

    override var name: String = "address"

    companion object {
        const val PREFIX = "1.16."
        const val CONTRACT_ADDRESS_SIZE = 40
        const val CONTRACT_ADDRESS_PREFIX = "01"
    }

    override fun encode(source: String): String {
        val encodeSource = if (source.startsWith(PREFIX)) source.removePrefix(PREFIX) else source

        return appendContractAddressPattern(
            CONTRACT_ADDRESS_PREFIX,
            convertToByteCode(BigDecimal(encodeSource).toBigInteger())
        )
    }

    private fun appendContractAddressPattern(prefix: String, value: String): String {
        val valueHash = HASH_PATTERN.substring(0, CONTRACT_ADDRESS_SIZE - value.length) + value
        val prefixedHash = prefix + valueHash.substring(prefix.length, valueHash.length)

        return HASH_PATTERN.substring(0, HASH_PATTERN.length - prefixedHash.length) + prefixedHash
    }

}


/**
 * Implementation of [InputValueType] for fixed bytes types
 */
class FixedBytesInputValueType(private val size: Int) : InputValueType {

    override var encodingContext: EncodingContext? = null

    override var name: String = "bytes$size"

    override fun encode(source: String): String {
        var value = Hex.encode(source.toByteArray())

        if (value.size > size) {
            value = value.copyOfRange(0, size)
        }

        val fillData = ByteArray(INPUT_SLICE_SIZE - value.size)
        value += fillData

        val stringData = convertToByteCode(source)

        return getStringHash(stringData)
    }

}

/**
 * Implementation of [InputValueType] for bytes types
 */
class BytesInputValueType : InputValueType {

    override var encodingContext: EncodingContext? = null

    override var name: String = "bytes"

    override fun encode(source: String): String {
        val currOffset = encodingContext!!.dynamicParametersOffset

        val length = source.toByteArray().count()

        with(encodingContext!!) {
            appendDynamicDataPart(appendStringPattern(source))
            dynamicParametersOffset =
                    encodingContext!!.dynamicParametersOffset + length + INPUT_SLICE_SIZE
        }

        return appendNumericPattern(convertToByteCode(currOffset))
    }

}

/**
 * Implementation of [InputValueType] for string types
 */
class StringInputValueType : InputValueType {

    override var encodingContext: EncodingContext? = null

    override var name: String = "string"

    override fun encode(source: String): String {
        val curOffset = encodingContext!!.dynamicParametersOffset
        encodingContext!!.dynamicParametersOffset = encodingContext!!.dynamicParametersOffset +
                getStringHash(source).length

        appendStringParameters(source)

        return appendNumericPattern(convertToByteCode(curOffset))
    }

    private fun appendStringParameters(source: String) {
        encodingContext!!.appendDynamicDataPart(appendStringPattern(source))
    }

}

/**
 * Implementation of [InputValueType] for dynamic arrays
 */
class DynamicArrayInputValueType(private val itemType: InputValueType) : InputValueType {

    override var encodingContext: EncodingContext? = null

    override var name: String = "${itemType.name}[]"

    override fun encode(source: String): String {
        itemType.encodingContext = encodingContext

        val currOffset = encodingContext!!.dynamicParametersOffset

        val parameters = source.toArrayParameters()
        val count = parameters.size

        encodingContext!!.dynamicParametersOffset =
                currOffset + count * INPUT_SLICE_SIZE + INPUT_SLICE_SIZE

        var encodedParameters = ""
        encodedParameters += appendNumericPattern(count.toString())

        parameters.forEach { parameter ->
            encodedParameters += itemType.encode(parameter)
        }

        encodingContext!!.appendDynamicDataPart(encodedParameters)

        return appendNumericPattern(convertToByteCode(currOffset))
    }

}

/**
 * Implementation of [InputValueType] for dynamic string array
 *
 * Overrode default [StringInputValueType] logic
 */
class DynamicStringArrayValueType : InputValueType {

    override var encodingContext: EncodingContext? = null

    override var name: String = "string[]"

    override fun encode(source: String): String {
        val currOffset = encodingContext!!.dynamicParametersOffset

        val parameters = source.toStringArrayParameters()

        val count = parameters.size

        encodingContext!!.dynamicParametersOffset =
                currOffset + count * INPUT_SLICE_SIZE + INPUT_SLICE_SIZE

        var encodedParameters = ""
        encodedParameters += appendNumericPattern(count.toString())

        parameters.forEach { parameter ->
            encodedParameters += appendStringPattern(parameter)
        }

        encodingContext!!.appendDynamicDataPart(encodedParameters)

        return appendNumericPattern(convertToByteCode(currOffset))
    }

}

/**
 * Implementation of [InputValueType] for fixed arrays
 */
class FixedArrayInputValueType(private val size: Int, private val itemType: InputValueType) :
    InputValueType {

    override var encodingContext: EncodingContext? = null

    override var name: String = "${itemType.name}[$size]"

    override fun encode(source: String): String {
        itemType.encodingContext = encodingContext

        val currOffset = encodingContext!!.dynamicParametersOffset

        val parameters = source.toArrayParameters()

        encodingContext!!.dynamicParametersOffset =
                currOffset + size * INPUT_SLICE_SIZE + INPUT_SLICE_SIZE

        var encodedParameters = ""
        encodedParameters += appendNumericPattern(size.toString())

        (0 until size).forEach { index ->
            encodedParameters += itemType.encode(parameters[index])
        }

        encodingContext!!.appendDynamicDataPart(encodedParameters)

        return appendNumericPattern(convertToByteCode(currOffset))
    }

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

private fun convertToByteCode(value: String): String {
    return Hex.toHexString(value.toByteArray(Charsets.UTF_8))
}

private fun String.toArrayParameters(): List<String> =
    this.removePrefix("[").removeSuffix("]").split(",")

private fun String.toStringArrayParameters(): List<String> {
    val regex = Pattern.compile("\"(\\.|[^\"])*\"")
    val matcher = regex.matcher(this)

    val parameters = mutableListOf<String>()
    while (matcher.find()) {
        (0 until matcher.groupCount()).forEach {
            parameters.add(matcher.group(it).removePrefix("\"").removeSuffix("\""))
        }
    }

    return parameters
}

private fun getStringHash(value: String): String {
    return if (value.length <= HASH_PATTERN.length) {
        formNotFullString(value)
    } else {
        val ost = value.length % HASH_PATTERN.length
        value + HASH_PATTERN.substring(
            0,
            HASH_PATTERN.length - ost
        )
    }
}

private fun formNotFullString(value: String): String {
    return value + HASH_PATTERN.substring(value.length)
}

/**
 * Appends string pattern to contract call parameters
 */
private fun appendStringPattern(_value: String): String {
    val value = convertToByteCode(_value)

    var fullParameter = ""
    fullParameter += getStringLength(_value)
    fullParameter += getStringHash(value)

    return fullParameter
}

private fun getStringLength(value: String): String =
    appendNumericPattern(convertToByteCode(value.length.toLong()))
