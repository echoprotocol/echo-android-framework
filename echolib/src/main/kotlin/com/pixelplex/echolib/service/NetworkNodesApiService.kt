package com.pixelplex.echolib.service

import com.pixelplex.echolib.ILLEGAL_ID

/**
 * Encapsulates logic, associated with blockchain network nodes API
 *
 * <p>
 *     Graphene blockchain network nodes API:
 *     http://docs.bitshares.org/api/network_node.html
 * </p>
 *
 * @author Dmitriy Bushuev
 */
interface NetworkNodesApiService : ApiService {

    companion object {
        /**
         * Actual id for NetworkNodesApi
         */
        @Volatile
        var id: Int = ILLEGAL_ID
    }
}
