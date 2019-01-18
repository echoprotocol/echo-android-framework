package org.echo.mobile.framework.core.crypto.internal.eddsa.key

import jp.co.soramitsu.crypto.ed25519.EdDSAPrivateKey
import jp.co.soramitsu.crypto.ed25519.EdDSAPublicKey
import jp.co.soramitsu.crypto.ed25519.spec.EdDSAGenParameterSpec
import jp.co.soramitsu.crypto.ed25519.spec.EdDSANamedCurveSpec
import jp.co.soramitsu.crypto.ed25519.spec.EdDSANamedCurveTable
import jp.co.soramitsu.crypto.ed25519.spec.EdDSAParameterSpec
import jp.co.soramitsu.crypto.ed25519.spec.EdDSAPrivateKeySpec
import jp.co.soramitsu.crypto.ed25519.spec.EdDSAPublicKeySpec
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidParameterException
import java.security.KeyPair
import java.security.KeyPairGeneratorSpi
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec
import java.util.Hashtable

/**
 * EdDSA25519 key pair generator
 *
 * Reimplemented generation method to use specified seed as private key (no random)
 *
 * @author Dmitriy Bushuev
 */
class KeyPairGenerator : KeyPairGeneratorSpi() {
    private var edParams: EdDSAParameterSpec? = null
    private var random: SecureRandom? = null
    private var initialized: Boolean = false
    var seed: ByteArray? = null

    override fun initialize(keysize: Int, random: SecureRandom) {
        val edParams = edParameters[keysize]
        if (edParams == null) {
            throw InvalidParameterException("unknown key type.")
        } else {
            try {
                this.initialize(edParams, random)
            } catch (var5: InvalidAlgorithmParameterException) {
                throw InvalidParameterException("key type not configurable.")
            }
        }
    }

    @Throws(InvalidAlgorithmParameterException::class)
    override fun initialize(params: AlgorithmParameterSpec, random: SecureRandom) {
        if (params is EdDSAParameterSpec) {
            this.edParams = params
        } else {
            if (params !is EdDSAGenParameterSpec) {
                throw InvalidAlgorithmParameterException("parameter object not a EdDSAParameterSpec")
            }
            this.edParams = this.createNamedCurveSpec(params.name)
        }

        this.random = random
        this.initialized = true
    }

    override fun generateKeyPair(): KeyPair {
        if (!this.initialized) {
            this.initialize(256, SecureRandom())
        }

        val seed: ByteArray = if (this.seed == null) {
            ByteArray(this.edParams!!.curve.field.getb() / 8)
        } else {
            this.seed!!
        }

        val privKey = EdDSAPrivateKeySpec(seed, this.edParams!!)
        val pubKey = EdDSAPublicKeySpec(privKey.a, this.edParams)
        return KeyPair(EdDSAPublicKey(pubKey), EdDSAPrivateKey(privKey))
    }

    /**
     * Specifies seed (private key) for key pair generation
     */
    fun seed(seed: ByteArray) {
        this.seed = seed
    }

    @Throws(InvalidAlgorithmParameterException::class)
    private fun createNamedCurveSpec(curveName: String): EdDSANamedCurveSpec {
        val spec = EdDSANamedCurveTable.getByName(curveName)
        return spec ?: throw InvalidAlgorithmParameterException("unknown curve name: $curveName")
    }

    companion object {
        private const val DEFAULT_KEYSIZE = 256
        private val edParameters = Hashtable<Int, AlgorithmParameterSpec>()

        init {
            edParameters[DEFAULT_KEYSIZE] = EdDSAGenParameterSpec("Ed25519")
        }
    }
}
