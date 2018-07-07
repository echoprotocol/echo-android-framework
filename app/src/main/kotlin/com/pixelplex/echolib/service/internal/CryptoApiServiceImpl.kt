package com.pixelplex.echolib.service.internal

import com.pixelplex.echolib.core.SocketCore
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
class CryptoApiServiceImpl(private val socketCore: SocketCore) : CryptoApiService
