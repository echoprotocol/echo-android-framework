package com.pixelplex.echolib.service.internal

import com.pixelplex.echolib.core.SocketCore
import com.pixelplex.echolib.service.NetworkBroadcastApiService

/**
 * Implementation of [NetworkBroadcastApiService]
 *
 * <p>
 *     Encapsulates logic of preparing API calls to [SocketCode]
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class NetworkBroadcastApiServiceImpl(private val socketCore: SocketCore) :
    NetworkBroadcastApiService
