package com.pixelplex.echolib.support

import com.pixelplex.echolib.model.Address

/**
 * @author Daria Pechkovskaya
 */
enum class NetworkType {
    MAIN_NET,
    TEST_NET
}

fun NetworkType.getAddressPrefix(): String =
    when (this) {
        NetworkType.MAIN_NET -> Address.BITSHARES_PREFIX
        NetworkType.TEST_NET -> Address.TESTNET_PREFIX
    }