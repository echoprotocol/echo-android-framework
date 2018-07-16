package com.pixelplex.echolib.service

import com.pixelplex.echolib.ILLEGAL_ID

/**
 * Encapsulates logic, associated with blockchain network broadcast API
 *
 * <p>
 *     Graphene blockchain network broadcast API:
 *     http://docs.bitshares.org/api/network_broadcast.html
 * </p>
 *
 * @author Dmitriy Bushuev
 */
interface NetworkBroadcastApiService : ApiService {

    companion object {
        /**
         * Actual id for NetworkBroadcastApi
         */
        @Volatile
        var id: Int = ILLEGAL_ID
    }
}
