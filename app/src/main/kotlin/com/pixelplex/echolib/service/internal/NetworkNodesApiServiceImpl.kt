package com.pixelplex.echolib.service.internal

import com.pixelplex.echolib.core.SocketCore
import com.pixelplex.echolib.service.NetworkNodesApiService

/**
 * Implementation of [NetworkNodesApiService]
 *
 * <p>
 *     Encapsulates logic of preparing API calls to [SocketCode]
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class NetworkNodesApiServiceImpl(private val socketCore: SocketCore) : NetworkNodesApiService
