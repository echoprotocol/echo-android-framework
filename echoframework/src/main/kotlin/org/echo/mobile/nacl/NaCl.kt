package org.echo.mobile.nacl

/**
 * This class exposes the methods from the NaCl library
 */
@Suppress("All")
object NaCl {

    const val CRYPTO_SECRET_BOX_KEYBYTES = 32
    const val CRYPTO_SECRET_BOX_NONCEBYTES = 24
    private const val CRYPTO_SECRET_BOX_ZEROBYTES = 32
    private const val CRYPTO_SECRET_BOX_BOXZEROBYTES = 16
    private const val CRYPTO_SCALARMULT_BYTES = 32
    private const val CRYPTO_SCALARMULT_SCALARBYTES = 32
    const val CRYPTO_BOX_PUBLICKEYBYTES = 32
    const val CRYPTO_BOX_SECRETKEYBYTES = 32
    const val CRYPTO_BOX_BEFORENMBYTES = 32

    private fun checkLengths(k: ByteArray, n: ByteArray) {
        require(k.size == CRYPTO_SECRET_BOX_KEYBYTES) { "bad key size" }
        require(n.size == CRYPTO_SECRET_BOX_NONCEBYTES) { "bad nonce size" }
    }

    private fun checkBoxLengths(pk: ByteArray, sk: ByteArray) {
        require(pk.size == CRYPTO_BOX_PUBLICKEYBYTES) { "bad public key size" }
        require(sk.size == CRYPTO_BOX_SECRETKEYBYTES) { "bad secret key size" }
    }

    object SecretBox {

        fun seal(msg: ByteArray, nonce: ByteArray, key: ByteArray): ByteArray {
            checkLengths(key, nonce)

            val m = ByteArray(CRYPTO_SECRET_BOX_ZEROBYTES + msg.size)
            val c = ByteArray(m.size)
            msg.copyInto(m, CRYPTO_SECRET_BOX_ZEROBYTES)

            NaClLowLevel.cryptoSecretBox(c, m, m.size.toLong(), nonce, key)
            return c.copyOfRange(CRYPTO_SECRET_BOX_BOXZEROBYTES, c.size)
        }

        fun open(box: ByteArray, nonce: ByteArray, key: ByteArray): ByteArray? {
            checkLengths(key, nonce)

            val ciphertext = ByteArray(CRYPTO_SECRET_BOX_BOXZEROBYTES + box.size)
            val msg = ByteArray(ciphertext.size)
            box.copyInto(ciphertext, CRYPTO_SECRET_BOX_BOXZEROBYTES)

            if (ciphertext.size < 32) {
                return null
            }
            if (NaClLowLevel.cryptoSecretBoxOpen(
                    msg,
                    ciphertext,
                    ciphertext.size.toLong(),
                    nonce,
                    key
                ) != 0
            ) {
                return null
            }
            return msg.copyOfRange(CRYPTO_SECRET_BOX_ZEROBYTES, msg.size)
        }
    }

    internal fun scalarMult(n: ByteArray, p: ByteArray): ByteArray {
        require(n.size == CRYPTO_SCALARMULT_SCALARBYTES) { "bad n size" }
        require(p.size == CRYPTO_SCALARMULT_BYTES) { "bad p size" }
        val q = ByteArray(CRYPTO_SCALARMULT_BYTES)
        NaClLowLevel.cryptoScalarMult(q, n, p)
        return q
    }

    object Box {

        internal fun before(publicKey: ByteArray, secretKey: ByteArray): ByteArray {
            checkBoxLengths(publicKey, secretKey)
            val k = ByteArray(CRYPTO_BOX_BEFORENMBYTES)
            NaClLowLevel.cryptoBoxBeforeNm(k, publicKey, secretKey)
            return k
        }

        fun seal(
            msg: ByteArray,
            nonce: ByteArray,
            publicKey: ByteArray,
            secretKey: ByteArray
        ): ByteArray {
            val k = before(publicKey, secretKey)
            return SecretBox.seal(msg, nonce, k)
        }

        fun open(
            msg: ByteArray,
            nonce: ByteArray,
            publicKey: ByteArray,
            secretKey: ByteArray
        ): ByteArray? {
            val k = before(publicKey, secretKey)
            return SecretBox.open(msg, nonce, k)
        }

        /**
         * Generates a new key pair
         */
        fun keyPair(): Pair<ByteArray, ByteArray> {
            val pk = ByteArray(CRYPTO_BOX_PUBLICKEYBYTES)
            val sk = ByteArray(CRYPTO_BOX_SECRETKEYBYTES)
            NaClLowLevel.cryptoBoxKeyPair(pk, sk)
            return (pk to sk)
        }

        /**
         * Derives a public key and returns a keypair of the form (publicKey, secretKey)
         */
        fun keyPairFromSecretKey(secretKey: ByteArray): Pair<ByteArray, ByteArray> {
            require(secretKey.size == CRYPTO_BOX_SECRETKEYBYTES) { "bad secret key size" }
            val pk = ByteArray(CRYPTO_BOX_PUBLICKEYBYTES)
            NaClLowLevel.cryptoScalarMultBase(pk, secretKey)
            return (pk to secretKey)
        }
    }

}