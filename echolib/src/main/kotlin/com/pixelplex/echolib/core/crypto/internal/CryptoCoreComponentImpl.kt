package com.pixelplex.echolib.core.crypto.internal

import com.pixelplex.bitcoinj.ECKey
import com.pixelplex.bitcoinj.Sha256Hash
import com.pixelplex.echolib.core.crypto.CryptoCoreComponent
import com.pixelplex.echolib.model.AuthorityType

/**
 * Implementation of [CryptoCoreComponent]
 *
 * @author Daria Pechkovskaya
 * @author Dmitriy Bushuev
 */
class CryptoCoreComponentImpl : CryptoCoreComponent {

    private val seedProvider = RoleDependentSeedProvider(AuthorityType.ACTIVE)
    private val ecKeyConverter = ECKeyToAddressConverter()

    override fun getAddress(userName: String, password: String): String {
        return ecKeyConverter.convert(getPrivateKey(userName, password))
    }

    override fun getPrivateKey(userName: String, password: String): ECKey {
        val seedString = generateSeed(userName, password)
        return ECKey.fromPrivate(createPrivateKey(seedString))
    }

    private fun generateSeed(userName: String, password: String) =
        seedProvider.provide(userName, password)

    private fun createPrivateKey(seed: String): ByteArray {
        val seedBytes = seed.toByteArray(Charsets.UTF_8)
        return Sha256Hash.hash(seedBytes)
    }

}
