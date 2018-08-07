@file:JvmName("CryptoUtils")

package com.pixelplex.echoframework.support.crypto

import com.pixelplex.echoframework.support.sha512hash
import org.spongycastle.crypto.engines.AESEngine
import org.spongycastle.crypto.modes.CBCBlockCipher
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher
import org.spongycastle.crypto.params.KeyParameter
import org.spongycastle.crypto.params.ParametersWithIV

/**
 * Contains useful utility functions connected with cryptography
 *
 * @author Dmitriy Bushuev
 */

private const val AES_BLOCK_SIZE = 16
private const val IV_SIZE = AES_BLOCK_SIZE
private const val SKS_SIZE = 32

/**
 * Function to encrypt [input] with padded AES algorithm
 *
 * Creates key seed (first 32 bytes of key) and iv (16 bytes after key seed) bytes from [key]
 *
 * @param input data to encrypt
 * @param key   key for encryption
 * @return      AES Encryption of input
 */
fun encryptAES(input: ByteArray, key: ByteArray): ByteArray? {
    val result = key.sha512hash()

    val ivBytes = ByteArray(IV_SIZE)
    System.arraycopy(result,
        SKS_SIZE, ivBytes, 0,
        IV_SIZE
    )
    val sksBytes = ByteArray(SKS_SIZE)
    System.arraycopy(result, 0, sksBytes, 0, SKS_SIZE)

    return encryptAESInternal(input, sksBytes, ivBytes)
}

private fun encryptAESInternal(input: ByteArray, sks: ByteArray, iv: ByteArray): ByteArray {
    val cipher = PaddedBufferedBlockCipher(CBCBlockCipher(AESEngine())).apply {
        init(true, ParametersWithIV(KeyParameter(sks), iv))
    }

    val out = ByteArray(cipher.getOutputSize(input.size))
    val processed = cipher.processBytes(input, 0, input.size, out, 0)
    cipher.doFinal(out, processed)

    return out
}

/**
 * Function to decrypt [input] with padded AES algorithm
 *
 * @param input data to decrypt
 * @param key   key for decryption
 * @return      input decrypted with AES. Null if the decrypt failed (Bad Key)
 */
fun decryptAES(input: ByteArray, key: ByteArray): ByteArray? {
    val result = key.sha512hash()

    val ivBytes = ByteArray(IV_SIZE)
    System.arraycopy(result,
        SKS_SIZE, ivBytes, 0,
        IV_SIZE
    )
    val sksBytes = ByteArray(SKS_SIZE)
    System.arraycopy(result, 0, sksBytes, 0, SKS_SIZE)

    return decryptAESInternal(input, sksBytes, ivBytes)
}

private fun decryptAESInternal(input: ByteArray, sks: ByteArray, iv: ByteArray): ByteArray {
    val cipher = PaddedBufferedBlockCipher(CBCBlockCipher(AESEngine())).apply {
        init(false, ParametersWithIV(KeyParameter(sks), iv))
    }

    val preOut = ByteArray(cipher.getOutputSize(input.size))
    val processed = cipher.processBytes(input, 0, input.size, preOut, 0)
    val postProcessed = cipher.doFinal(preOut, processed)
    val out = ByteArray(processed + postProcessed)
    System.arraycopy(preOut, 0, out, 0, processed + postProcessed)

    return out
}
