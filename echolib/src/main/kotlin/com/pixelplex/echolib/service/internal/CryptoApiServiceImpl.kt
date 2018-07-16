package com.pixelplex.echolib.service.internal

import com.pixelplex.echolib.core.socket.SocketCoreComponent
import com.pixelplex.echolib.service.CryptoApiService

/**
 * Implementation of [CryptoApiService]
 *
 * <p>
 *     Encapsulates logic of preparing API calls to [SocketCode]
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class CryptoApiServiceImpl(private val socketCoreComponent: SocketCoreComponent) : CryptoApiService
