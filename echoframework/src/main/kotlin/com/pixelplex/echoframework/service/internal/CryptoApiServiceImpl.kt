package com.pixelplex.echoframework.service.internal

import com.pixelplex.echoframework.ILLEGAL_ID
import com.pixelplex.echoframework.core.socket.SocketCoreComponent
import com.pixelplex.echoframework.service.CryptoApiService

/**
 * Implementation of [CryptoApiService]
 *
 * <p>
 *     Encapsulates logic of preparing API calls to [SocketCode]
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class CryptoApiServiceImpl(private val socketCoreComponent: SocketCoreComponent) :
    CryptoApiService {

    override var id: Int = ILLEGAL_ID

}
