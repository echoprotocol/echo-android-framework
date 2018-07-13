package com.pixelplex.echolib.support

import com.pixelplex.echolib.facade.InitializerFacade

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
        Api.ACCOUNT_HISTORY -> InitializerFacade.accountHistoryApiId
        Api.CRYPTO -> InitializerFacade.cryptoApiId
        Api.DATABASE -> InitializerFacade.databaseApiId
        Api.NETWORK_BROADCAST -> InitializerFacade.networkBroadcastApiId
        Api.NETWORK_NODES -> InitializerFacade.networkNodesApiId
    }
