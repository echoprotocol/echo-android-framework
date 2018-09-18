/*
 * Copyright 2011 Google Inc.
 * Copyright 2014 Andreas Schildbach
 * Copyright 2014-2016 the libsecp256k1 contributors
 * Copyright 2018 Bushuev Dmitriy and Dasha Pechkovskaya
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pixelplex.bitcoinj

import com.google.common.annotations.VisibleForTesting
import com.google.common.base.Objects
import com.google.common.base.Preconditions
import com.google.common.base.Preconditions.checkArgument
import com.google.common.base.Preconditions.checkNotNull
import org.spongycastle.asn1.pkcs.EncryptedData
import org.spongycastle.asn1.x9.X9IntegerConverter
import org.spongycastle.crypto.digests.SHA256Digest
import org.spongycastle.crypto.ec.CustomNamedCurves
import org.spongycastle.crypto.generators.ECKeyPairGenerator
import org.spongycastle.crypto.params.ECDomainParameters
import org.spongycastle.crypto.params.ECKeyGenerationParameters
import org.spongycastle.crypto.params.ECPrivateKeyParameters
import org.spongycastle.crypto.params.ECPublicKeyParameters
import org.spongycastle.crypto.signers.ECDSASigner
import org.spongycastle.crypto.signers.HMacDSAKCalculator
import org.spongycastle.math.ec.ECAlgorithms
import org.spongycastle.math.ec.ECPoint
import org.spongycastle.math.ec.FixedPointCombMultiplier
import org.spongycastle.math.ec.FixedPointUtil
import org.spongycastle.math.ec.custom.sec.SecP256K1Curve
import java.math.BigInteger
import java.security.SecureRandom


/**
 * Represents an elliptic curve public and (optionally) private key, usable for digital signatures but not encryption.
 * Creating a new ECKey with the empty constructor will generate a new random keypair. Other static methods can be used
 * when you already have the public or private parts. If you create a key with only the public part, you can check
 * signatures but not create them.
 *
 * ECKey also provides access to Bitcoin Core compatible text message signing, as accessible via the UI or JSON-RPC.
 * This is slightly different to signing raw bytes - if you want to sign your own data and it won't be exposed as
 * text to people, you don't want to use this. If in doubt, ask on the mailing list.
 *
 * The ECDSA algorithm supports key recovery in which a signature plus a couple of discriminator bits can
 * be reversed to find the public key used to calculate it. This can be convenient when you have a message and a
 * signature and want to find out who signed it, rather than requiring the user to provide the expected identity.
 *
 * This class supports a variety of serialization forms. The methods that accept/return byte arrays serialize
 * private keys as raw byte arrays and public keys using the SEC standard byte encoding for public keys. Signatures
 * are encoded using ASN.1/DER inside the Bitcoin protocol.
 *
 * A key can be compressed or uncompressed. This refers to whether the public key is represented
 * when encoded into bytes as an (x, y) coordinate on the elliptic curve, or whether it's represented as just an X
 * co-ordinate and an extra byte that carries a sign bit. With the latter form the Y coordinate can be calculated
 * dynamically, however, because the binary serialization is different the address of a key changes if its
 * compression status is changed.
 * If you deviate from the defaults it's important to understand this: money sent
 * to a compressed version of the key will have a different address to the same key in uncompressed form. Whether
 * a public key is compressed or not is recorded in the SEC binary serialisation format, and preserved in a flag in
 * this class so round-tripping preserves state. Unless you're working with old software or doing unusual things, you
 * can usually ignore the compressed/uncompressed distinction.
 *
 * @author Daria Pechkovskaya
 */
class ECKey {
    val priv: BigInteger?
    val pub: LazyECPoint

    constructor(priv: BigInteger?, pub: ECPoint) : this(
        priv,
        LazyECPoint(checkNotNull(pub))
    )

    constructor(priv: BigInteger?, pub: LazyECPoint) {
        if (priv != null) {
            checkArgument(
                priv.bitLength() <= 32 * 8,
                "private key exceeds 32 bytes: %s bits",
                priv.bitLength()
            )
            // Try and catch buggy callers or bad key imports, etc. Zero and one are special because these are often
            // used as sentinel values and because scripting languages have a habit of auto-casting true and false to
            // 1 and 0 or vice-versa. Type confusion bugs could therefore result in private keys with these values.
            checkArgument(priv != BigInteger.ZERO)
            checkArgument(priv != BigInteger.ONE)
        }
        this.priv = priv
        checkNotNull(pub)
        this.pub = pub
    }

    // Creation time of the key in seconds since the epoch, or zero if the key was deserialized from a version that did
    // not have this field.
    protected var creationTimeSeconds: Long = 0

    protected var encryptedPrivateKey: EncryptedData? = null

    private val pubKeyHash: ByteArray? = null

    /**
     * Generates an entirely new keypair. Point compression is used so the resulting public key will be 33 bytes
     * (32 for the co-ordinate and 1 byte to represent the y bit).
     */
    constructor() : this(secureRandom)

    /**
     * Generates an entirely new keypair with the given [SecureRandom] object. Point compression is used so the
     * resulting public key will be 33 bytes (32 for the co-ordinate and 1 byte to represent the y bit).
     */
    constructor(secureRandom: SecureRandom) {
        val generator = ECKeyPairGenerator()
        val keygenParams = ECKeyGenerationParameters(curve, secureRandom)
        generator.init(keygenParams)
        val keypair = generator.generateKeyPair()
        val privParams = keypair.private as ECPrivateKeyParameters
        val pubParams = keypair.public as ECPublicKeyParameters
        priv = privParams.d
        pub = LazyECPoint(curve.curve, pubParams.q.getEncoded(true))
    }

    /**
     * Returns a copy of this key, but with the public point represented in uncompressed form. Normally you would
     * never need this: it's for specialised scenarios or when backwards compatibility in encoded form is necessary.
     */
    fun decompress(): ECKey {
        return if (!pub.isCompressed)
            this
        else
            ECKey(priv, decompressPoint(pub.get()))
    }

    /**
     * Utility for decompressing an elliptic curve point. Returns the same point if it's already compressed.
     * See the ECKey class docs for a discussion of point compression.
     */
    fun decompressPoint(point: ECPoint): ECPoint {
        return getPointWithCompression(point, false)
    }

    /**
     * Utility for decompressing an elliptic curve point. Returns the same point if it's already compressed.
     * See the ECKey class docs for a discussion of point compression.
     */
    fun decompressPoint(point: LazyECPoint): LazyECPoint {
        return if (!point.isCompressed) point else LazyECPoint(decompressPoint(point.get()))
    }

    /**
     * Returns true if this key has unencrypted access to private key bytes. Does the opposite of
     * [.isPubKeyOnly].
     */
    fun hasPrivKey(): Boolean {
        return priv != null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is ECKey) return false
        return (Objects.equal(this.priv, other.priv)
                && Objects.equal(this.pub, other.pub)
                && Objects.equal(this.creationTimeSeconds, other.creationTimeSeconds)
                && Objects.equal(this.encryptedPrivateKey, other.encryptedPrivateKey))
    }

    override fun hashCode() = pub.hashCode()

    /**
     * Returns the recovery ID, a byte with value between 0 and 3, inclusive, that specifies which of 4 possible
     * curve points was used to sign a message. This value is also referred to as "v".
     *
     * @throws RuntimeException if no recovery ID can be found.
     */
    fun findRecoveryId(hash: Sha256Hash, sig: ECDSASignature): Byte {
        var recId: Byte = -1
        for (i in 0..3) {
            val k = ECKey.recoverFromSignature(i, sig, hash, isCompressed)
            if (k != null && k.pub == pub) {
                recId = i.toByte()
                break
            }
        }
        if (recId.toInt() == -1)
            throw RuntimeException("Could not construct a recoverable key. This should never happen.")
        return recId
    }

    /**
     * Returns hex representation of private key
     */
    fun getPrivateKeyAsHex(): String {
        return HEX.encode(getPrivKeyBytes())
    }

    /**
     * Returns a 32 byte array containing the private key.
     *
     * @throws [com.pixelplex.bitcoinj.MissingPrivateKeyException] if the private key bytes are missing/encrypted.
     */
    fun getPrivKeyBytes(): ByteArray =
        priv?.bigIntegerToBytes(32) ?: throw MissingPrivateKeyException()

    companion object {

        @JvmField
        var curve: ECDomainParameters
        private val curveParams = CustomNamedCurves.getByName("secp256k1")

        /**
         * Equal to curve.getN().shiftRight(1), used for canonicalising the S value of a signature. If you aren't
         * sure what this is about, you can ignore it.
         */
        @JvmField
        val HALF_CURVE_ORDER: BigInteger

        /**
         * If this global variable is set to true, sign() creates a dummy signature and verify() always returns true.
         * This is intended to help accelerate unit tests that do a lot of signing/verifying, which in the debugger
         * can be painfully slow.
         */
        @VisibleForTesting
        var FAKE_SIGNATURES = false

        private const val CURVE_MIN_WIDTH = 12

        private var secureRandom: SecureRandom

        init {
            // Init proper random number generator, as some old Android installations have bugs that make it unsecure.
            if (isAndroidRuntime)
                LinuxSecureRandom()

            // Tell Bouncy Castle to precompute data that's needed during secp256k1 calculations. Increasing the width
            // number makes calculations faster, but at a cost of extra memory usage and with decreasing returns. 12 was
            // picked after consulting with the BC team.
            FixedPointUtil.precompute(curveParams.g, CURVE_MIN_WIDTH)
            curve = ECDomainParameters(
                curveParams.curve, curveParams.g, curveParams.n,
                curveParams.h
            )
            HALF_CURVE_ORDER = curveParams.n.shiftRight(1)

            // Init proper random number generator, as some old Android installations have bugs that make it unsecure.
            if (isAndroidRuntime)
                LinuxSecureRandom()

            // Tell Bouncy Castle to precompute data that's needed during secp256k1 calculations. Increasing the width
            // number makes calculations faster, but at a cost of extra memory usage and with decreasing returns. 12 was
            // picked after consulting with the BC team.
            FixedPointUtil.precompute(curveParams.g, 12)
            curve = ECDomainParameters(
                curveParams.curve, curveParams.g, curveParams.n,
                curveParams.h
            )
            secureRandom = SecureRandom()
        }

        /**
         * Creates an ECKey given the private key only. The public key is calculated from it (this is slow). The resulting
         * public key is compressed.
         */
        @JvmStatic
        fun fromPrivate(privKeyBytes: ByteArray): ECKey {
            return fromPrivate(BigInteger(1, privKeyBytes))
        }

        /**
         * Creates an ECKey given the private key only. The public key is calculated from it (this is slow), either
         * compressed or not.
         */
        @JvmStatic
        fun fromPrivate(privKey: BigInteger): ECKey {
            return fromPrivate(privKey, true)
        }

        /**
         * Creates an ECKey given the private key only. The public key is calculated from it (this is slow). The resulting
         * public key is compressed.
         */
        @JvmStatic
        fun fromPrivate(privKey: BigInteger, compressed: Boolean): ECKey {
            val point = publicPointFromPrivate(privKey)
            return ECKey(
                privKey,
                getPointWithCompression(point, compressed)
            )
        }

        /**
         * Returns true if the given pubkey is canonical, i.e. the correct length taking into account compression.
         */
        @JvmStatic
        @SuppressWarnings("ReturnCount")
        fun isPubKeyCanonical(pubkey: ByteArray): Boolean {
            if (pubkey.size < 33)
                return false
            if (pubkey[0].toInt() == 0x04) {
                // Uncompressed pubkey
                if (pubkey.size != 65)
                    return false
            } else if (pubkey[0].toInt() == 0x02 || pubkey[0].toInt() == 0x03) {
                // Compressed pubkey
                if (pubkey.size != 33)
                    return false
            } else
                return false
            return true
        }

        /**
         * Creates an ECKey that cannot be used for signing, only verifying signatures, from the given point. The
         * compression state of pub will be preserved.
         */
        @JvmStatic
        fun fromPublicOnly(pub: ECPoint): ECKey {
            return ECKey(null, pub)
        }

        /**
         * Creates an ECKey that cannot be used for signing, only verifying signatures, from the given encoded point.
         * The compression state of pub will be preserved.
         */
        @JvmStatic
        fun fromPublicOnly(pub: ByteArray): ECKey {
            return ECKey(
                null,
                curve.curve.decodePoint(pub)
            )
        }

        /**
         * Returns public key point from the given private key. To convert a byte array into a BigInteger, use <tt>
         * new BigInteger(1, bytes);</tt>
         */
        @JvmStatic
        fun publicPointFromPrivate(privKey: BigInteger): ECPoint {
            var key = privKey
            if (privKey.bitLength() > curve.n.bitLength()) {
                key = privKey.mod(curve.n)
            }
            return FixedPointCombMultiplier().multiply(curve.g, key)
        }

        private fun getPointWithCompression(point: ECPoint, compressed: Boolean): ECPoint {
            if (point.isCompressed == compressed) return point

            val newPoint = point.normalize()
            val x = newPoint.affineXCoord.toBigInteger()
            val y = newPoint.affineYCoord.toBigInteger()
            return curve.curve.createPoint(x, y, compressed)
        }

        /**
         * Given the components of a signature and a selector value, recover and return the public key
         * that generated the signature according to the algorithm in SEC1v2 section 4.1.6.
         *
         * The recId is an index from 0 to 3 which indicates which of the 4 possible keys is the correct one. Because
         * the key recovery operation yields multiple potential keys, the correct key must either be stored alongside the
         * signature, or you must be willing to try each recId in turn until you find one that outputs the key you are
         * expecting.
         *
         * If this method returns null it means recovery was not possible and recId should be iterated.
         *
         * Given the above two points, a correct usage of this method is inside a for loop from 0 to 3, and if the
         * output is null OR a key that is not the one you expect, you try again with the next recId.
         *
         * @param recId      Which possible key to recover.
         * @param sig        the R and S components of the signature, wrapped.
         * @param message    Hash of the data that was signed.
         * @param compressed Whether or not the original pubkey was compressed.
         * @return An EcKey containing only the public part, or null if recovery wasn't possible.
         */
        @JvmStatic
        fun recoverFromSignature(
            recId: Int,
            sig: ECDSASignature,
            message: Sha256Hash,
            compressed: Boolean
        ): ECKey? {
            Preconditions.checkArgument(recId >= 0, "recId must be positive")
            Preconditions.checkArgument(sig.r.signum() >= 0, "r must be positive")
            Preconditions.checkArgument(sig.s.signum() >= 0, "s must be positive")
            Preconditions.checkNotNull(message)
            // 1.0 For j from 0 to h   (h == recId here and the loop is outside this function)
            //   1.1 Let x = r + jn
            val n = curve.n  // Curve order.
            val i = BigInteger.valueOf(recId.toLong() / 2)
            val x = sig.r.add(i.multiply(n))
            //   1.2. Convert the integer x to an octet string X of length mlen using the conversion routine
            //        specified in Section 2.3.7, where mlen = ⌈(log2 p)/8⌉ or mlen = ⌈m/8⌉.
            //   1.3. Convert the octet string (16 set binary digits)||X to an elliptic curve point R using the
            //        conversion routine specified in Section 2.3.4. If this conversion routine outputs “invalid”, then
            //        do another iteration of Step 1.
            //
            // More concisely, what these points mean is to use X as a compressed public key.
            val prime = SecP256K1Curve.q
            if (x >= prime) {
                // Cannot have point co-ordinates larger than this as everything takes place modulo Q.
                return null
            }
            // Compressed keys require you to know an extra bit of data about the y-coord as there are two possibilities.
            // So it's encoded in the recId.
            val R = decompressKey(x, recId and 1 == 1)
            //   1.4. If nR != point at infinity, then do another iteration of Step 1 (callers responsibility).
            if (!R.multiply(n).isInfinity)
                return null
            //   1.5. Compute e from M using Steps 2 and 3 of ECDSA signature verification.
            val e = message.toBigInteger()
            //   1.6. For k from 1 to 2 do the following.   (loop is outside this function via iterating recId)
            //   1.6.1. Compute a candidate public key as:
            //               Q = mi(r) * (sR - eG)
            //
            // Where mi(x) is the modular multiplicative inverse. We transform this into the following:
            //               Q = (mi(r) * s ** R) + (mi(r) * -e ** G)
            // Where -e is the modular additive inverse of e, that is z such that z + e = 0 (mod n). In the above equation
            // ** is point multiplication and + is point addition (the EC group operator).
            //
            // We can find the additive inverse by subtracting e from zero then taking the mod. For example the additive
            // inverse of 3 modulo 11 is 8 because 3 + 8 mod 11 = 0, and -3 mod 11 = 8.
            val eInv = BigInteger.ZERO.subtract(e).mod(n)
            val rInv = sig.r.modInverse(n)
            val srInv = rInv.multiply(sig.s).mod(n)
            val eInvrInv = rInv.multiply(eInv).mod(n)
            val q = ECAlgorithms.sumOfTwoMultiplies(curve.g, eInvrInv, R, srInv)
            return fromPublicOnly(q.getEncoded(compressed))
        }

        /**
         * Decompress a compressed public key (x co-ord and low-bit of y-coord).
         */
        private fun decompressKey(xBN: BigInteger, yBit: Boolean): ECPoint {
            val x9 = X9IntegerConverter()
            val compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(curve.curve))
            val yBitValue = 0x03
            compEnc[0] = (if (yBit) yBitValue else 0x02).toByte()
            return curve.curve.decodePoint(compEnc)
        }

        /**
         * Utility for compressing an elliptic curve point. Returns the same point if it's already compressed.
         * See the ECKey class docs for a discussion of point compression.
         */
        @JvmStatic
        fun compressPoint(point: ECPoint): ECPoint {
            return getPointWithCompression(point, true)
        }
    }

    val pubKey: ByteArray
        get() = pub.encoded

    val pubKeyPoint: ECPoint
        get() = pub.get()

    val isCompressed: Boolean
        get() = pub.isCompressed

    /**
     * Signs the given hash and returns the R and S components as BigIntegers. In the Bitcoin protocol, they are
     * usually encoded using ASN.1 format, so you want [ECKey.ECDSASignature.toASN1]
     * instead. However sometimes the independent components can be useful, for instance, if you're going to do
     * further EC maths on them.
     *
     * @throws KeyCrypterException if this ECKey doesn't have a private part.
     */
    @Throws(KeyCrypterException::class)
    fun sign(input: Sha256Hash): ECDSASignature {
        if (priv == null)
            throw MissingPrivateKeyException()
        return doSign(input, priv)
    }

    protected fun doSign(input: Sha256Hash, privateKeyForSigning: BigInteger): ECDSASignature {
//        if (FAKE_SIGNATURES) return TransactionSignature.dummy()

        kotlin.checkNotNull(privateKeyForSigning)
        val signer = ECDSASigner(HMacDSAKCalculator(SHA256Digest()))
        val privKey = ECPrivateKeyParameters(
            privateKeyForSigning,
            curve
        )
        signer.init(true, privKey)
        val components = signer.generateSignature(input.bytes)
        return ECDSASignature(components[0], components[1]).toCanonicalised()
    }

}
