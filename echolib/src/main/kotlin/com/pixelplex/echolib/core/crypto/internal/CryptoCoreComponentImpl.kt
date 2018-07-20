package com.pixelplex.echolib.core.crypto.internal

import com.pixelplex.bitcoinj.ECKey
import com.pixelplex.bitcoinj.Sha256Hash
import com.pixelplex.echolib.core.crypto.CryptoCoreComponent
import com.pixelplex.echolib.model.AuthorityType
import com.pixelplex.echolib.model.network.Network


/**
 * Implementation of [CryptoCoreComponent]
 *
 * @author Daria Pechkovskaya
 * @author Dmitriy Bushuev
 */
class CryptoCoreComponentImpl(network: Network) : CryptoCoreComponent {

    private val seedProvider = RoleDependentSeedProvider()
    private val ecKeyConverter = ECKeyToAddressConverter(network.addressPrefix)

    override fun getAddress(
        userName: String,
        password: String,
        authorityType: AuthorityType
    ): String {
        return ecKeyConverter.convert(getPrivateKey(userName, password, authorityType))
    }

    override fun getPrivateKey(
        userName: String,
        password: String,
        authorityType: AuthorityType
    ): ECKey {
        val seedString = generateSeed(userName, password, authorityType)
        return ECKey.fromPrivate(createPrivateKey(seedString))
    }

    private fun generateSeed(userName: String, password: String, authorityType: AuthorityType) =
        seedProvider.provide(userName, password, authorityType)

    private fun createPrivateKey(seed: String): ByteArray {
        val seedBytes = seed.toByteArray(Charsets.UTF_8)
        return Sha256Hash.hash(seedBytes)
    }
}
