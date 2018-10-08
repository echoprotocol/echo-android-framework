package org.echo.mobile.framework.service.internal

import org.echo.mobile.framework.ILLEGAL_ID
import org.echo.mobile.framework.core.socket.SocketCoreComponent
import org.echo.mobile.framework.service.CryptoApiService

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
