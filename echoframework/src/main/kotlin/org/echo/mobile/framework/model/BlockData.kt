package org.echo.mobile.framework.model

/**
 * This class encapsulates all block-related information needed in order to build a valid transaction.
 *
 * @author Daria Pechkovskaya
 */
class BlockData : ByteSerializable {

    var refBlockNum: Int
    var refBlockPrefix: Long
    var relativeExpiration: Long

    /**
     * Block data constructor
     *
     * @param refBlockNum:        Least significant 16 bits from the reference block number.
     *                            If "relative_expiration" is zero, this field must be zero as well.
     * @param refBlockPrefix:     The first non-block-number 32-bits of the reference block ID.
     *                            Recall that block IDs have 32 bits of block number followed by the
     *                            actual block hash, so this field should be set using the second 32 bits
     *                            in the block_id_type
     * @param relativeExpiration: Expiration time specified as a POSIX or
     *                            [Unix time](https://en.wikipedia.org/wiki/Unix_time)
     */
    constructor(refBlockNum: Int, refBlockPrefix: Long, relativeExpiration: Long) {
        this.refBlockNum = refBlockNum
        this.refBlockPrefix = refBlockPrefix
        this.relativeExpiration = relativeExpiration
    }

    constructor(headBlockNumber: Long, headBlockId: String, relativeExpiration: Long) {
        this.refBlockNum = toRefBlockNum(headBlockNumber)
        this.refBlockPrefix = toRefBlockPrefix(headBlockId)
        this.relativeExpiration = relativeExpiration
    }

    /**
     * Converter that receives the block number, and takes the 16 lower bits of it to obtain the
     * 'refBlockNum' value.
     *
     * @param blockNumber: The block number.
     */
    private fun toRefBlockNum(blockNumber: Long) =
        blockNumber.toInt() and REF_BLOCK_NUM_BITS

    /**
     * Converter that receives the head block id, and turns it into the little format required for the
     * 'refBlockPrefix' field.
     *
     * @param headBlockId: The head block id as obtained from the network updates.
     */
    private fun toRefBlockPrefix(headBlockId: String): Long {
        val hashData = headBlockId.substring(HEAD_HASH_START, HEAD_HASH_END)
        var prefixData = ""
        for (i in 0 until HEAD_HASH_BYTES step HEAD_HASH_STEP) {
            prefixData += hashData.substring(
                HEAD_HASH_BYTES - HEAD_HASH_STEP - i,
                HEAD_HASH_BYTES - i
            )
        }
        return prefixData.toLong(REF_BLOCK_PREFIX_RADIX)
    }

    /** Allocating a fixed length byte array, since we will always need
     * 2 bytes for the ref_block_num value
     * 4 bytes for the ref_block_prefix value
     * 4 bytes for the relative_expiration
     */
    override fun toBytes(): ByteArray {
        val result =
            ByteArray(REF_BLOCK_NUM_BYTES + REF_BLOCK_PREFIX_BYTES + REF_BLOCK_EXPIRATION_BYTES)

        for (i in 0 until result.size) {
            when (i) {
                in 0 until REF_BLOCK_NUM_BYTES -> result[i] = (refBlockNum shr OFFSET * i).toByte()
                in REF_BLOCK_NUM_BYTES until REF_BLOCK_PREFIX_END -> result[i] =
                    (refBlockPrefix shr OFFSET * (i - REF_BLOCK_NUM_BYTES)).toByte()
                else -> result[i] =
                    (relativeExpiration shr OFFSET * (i - REF_BLOCK_PREFIX_END)).toByte()
            }
        }
        return result
    }

    companion object {
        private const val REF_BLOCK_NUM_BYTES = 2
        private const val REF_BLOCK_PREFIX_BYTES = 4
        private const val REF_BLOCK_EXPIRATION_BYTES = 4

        private const val HEAD_HASH_BYTES = 8
        private const val HEAD_HASH_STEP = 2
        const val HEAD_HASH_START = 8
        const val HEAD_HASH_END = HEAD_HASH_START + HEAD_HASH_BYTES

        const val REF_BLOCK_PREFIX_RADIX = 16

        const val REF_BLOCK_PREFIX_END = REF_BLOCK_NUM_BYTES + REF_BLOCK_PREFIX_BYTES

        const val REF_BLOCK_NUM_BITS = 0xFFFF

        const val OFFSET = 8
    }

}
