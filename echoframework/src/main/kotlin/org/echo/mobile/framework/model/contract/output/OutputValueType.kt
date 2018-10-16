package org.echo.mobile.framework.model.contract.output

import org.spongycastle.util.encoders.Hex

private const val NUMBER_RADIX = 16
private const val SLICE_SIZE = 64

/**
 * Describes functionality for contract result decoder
 *
 * @author Dmitriy Bushuev
 */
interface OutputValueType {

    /**
     * Decodes and trims [source] according to required value type
     */
    fun decode(source: ByteArray): Pair<Any, ByteArray>

}

/**
 * Implementation of [OutputValueType] for number types
 */
open class NumberOutputValueType : OutputValueType {

    override fun decode(source: ByteArray): Pair<Any, ByteArray> {
        val sourceSlice = source.take(SLICE_SIZE)
        val value = String(sourceSlice.toByteArray()).toLong(NUMBER_RADIX)
        return Pair(value, source.copyOfRange(SLICE_SIZE, source.size))
    }

}

/**
 * Extension of [NumberOutputValueType] for boolean type
 *
 * 1 - true
 * else - false
 */
class BooleanOutputValueType : NumberOutputValueType() {

    override fun decode(source: ByteArray): Pair<Any, ByteArray> {
        val decoded = super.decode(source)

        val decodedInt = decoded.first.toString().toInt()
        val resultSource = decoded.second

        return Pair(decodedInt == 1, resultSource)
    }

}

/**
 * Implementation of [OutputValueType] for string types
 *
 * Decodes input source into string representation according to offset and length values
 */
class StringOutputValueType : OutputValueType {

    override fun decode(source: ByteArray): Pair<Any, ByteArray> {
        val offset = String(source).slice(0 until SLICE_SIZE).toInt(NUMBER_RADIX) * 2
        val length = String(source).slice(SLICE_SIZE until SLICE_SIZE * 2).toInt(NUMBER_RADIX) * 2

        val result =
            String(Hex.decode(String(source).slice(SLICE_SIZE + offset until (SLICE_SIZE * 2 + length))))

        val start = SLICE_SIZE + countSlicesOfData(result.toByteArray()) * SLICE_SIZE

        return Pair(
            result,
            source.copyOfRange(start, source.size)
        )
    }

    private fun countSlicesOfData(data: ByteArray): Int {
        return if (data.size % SLICE_SIZE == 0) data.size / SLICE_SIZE else data.size / SLICE_SIZE + 1
    }

}


/**
 * Implementation of [OutputValueType] for фввкуыы types
 */
class AddressOutputValueType : OutputValueType {

    override fun decode(source: ByteArray): Pair<Any, ByteArray> {
        val address = String(source.copyOfRange(0, SLICE_SIZE)).toLong(NUMBER_RADIX)

        return Pair(address, source.copyOfRange(SLICE_SIZE, source.size))
    }

}

/**
 * Implementation of [OutputValueType] for list of one type
 *
 * Every list source should consist of 64 byte of count value and list items
 */
class ListValueType(private val itemType: OutputValueType) : OutputValueType {

    override fun decode(source: ByteArray): Pair<Any, ByteArray> {
        val count = listSize(source)

        val listSource = source.copyOfRange(SLICE_SIZE * 2, source.size)

        return processList(listSource, count)
    }

    private fun listSize(source: ByteArray) =
        String(source.copyOfRange(SLICE_SIZE, SLICE_SIZE * 2)).toInt(NUMBER_RADIX)

    private fun processList(listSource: ByteArray, count: Int): Pair<List<Any>, ByteArray> {
        var sourceResult = listSource
        val result = mutableListOf<Any>()
        (0 until count).forEach {
            val candidate = itemType.decode(sourceResult)
            result.add(candidate.first)
            sourceResult = candidate.second
        }

        return Pair(result, sourceResult)
    }

}
