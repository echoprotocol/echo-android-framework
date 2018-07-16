package com.pixelplex.echolib.support

import com.pixelplex.echolib.service.*

/**
 * Apis, needed for connection to blockchain
 *
 * @author Daria Pechkovskaya
 */
enum class Api {
    ACCOUNT_HISTORY,
    CRYPTO,
    DATABASE,
    NETWORK_BROADCAST,
    NETWORK_NODES
}

/**
 * Returns id from blockchain by api type
 */
fun Api.getId(): Int =
    when (this) {
        Api.ACCOUNT_HISTORY -> AccountHistoryApiService.id
        Api.CRYPTO -> CryptoApiService.id
        Api.DATABASE -> DatabaseApiService.id
        Api.NETWORK_BROADCAST -> NetworkBroadcastApiService.id
        Api.NETWORK_NODES -> NetworkNodesApiService.id
    }

/**
 * Returns id from blockchain by api type
 */
fun Api.updateId(id: Int) {
    when (this) {
        Api.ACCOUNT_HISTORY -> AccountHistoryApiService.id = id
        Api.CRYPTO -> CryptoApiService.id
        Api.DATABASE -> DatabaseApiService.id = id
        Api.NETWORK_BROADCAST -> NetworkBroadcastApiService.id = id
        Api.NETWORK_NODES -> NetworkNodesApiService.id = id
    }
}
