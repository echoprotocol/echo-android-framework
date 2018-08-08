package com.pixelplex.echoframework.support.crypto

/**
 * The parameters defining the standard FIPS 202.
 *
 * @author Daria Pechkovskaya
 */
enum class Parameter private constructor(val r: Int, val outputLength: Int, val d: String) {

    KECCAK_224(1152, 28, "01"),
    KECCAK_256(1088, 32, "01"),
    KECCAK_384(832, 48, "01"),
    KECCAK_512(576, 64, "01"),
    SHA3_224(1152, 28, "06"),
    SHA3_256(1088, 32, "06"),
    SHA3_384(832, 48, "06"),
    SHA3_512(576, 64, "06"),
    SHAKE128(1344, 32, "1F"),
    SHAKE256(1088, 64, "1F")
}
