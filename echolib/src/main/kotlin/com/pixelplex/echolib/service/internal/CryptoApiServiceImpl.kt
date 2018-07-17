package com.pixelplex.echolib.service.internal

import com.pixelplex.echolib.core.socket.SocketCoreComponent
import com.pixelplex.echolib.service.CryptoApiService
import com.pixelplex.echolib.support.Api

/**
 * Implementation of [CryptoApiService]
 *
 * <p>
 *     Encapsulates logic of preparing API calls to [SocketCode]
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class CryptoApiServiceImpl(private val socketCoreComponent: SocketCoreComponent) : CryptoApiService{

    override val api: Api = Api.CRYPTO
}
