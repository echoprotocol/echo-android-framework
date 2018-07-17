package com.pixelplex.echolib.service.internal

import com.pixelplex.echolib.core.crypto.CryptoCoreComponent
import com.pixelplex.echolib.core.socket.SocketCoreComponent
import com.pixelplex.echolib.service.AccountHistoryApiService
import com.pixelplex.echolib.support.Api

/**
 * Implementation of [AccountHistoryApiService]
 *
 * <p>
 *     Encapsulates logic of preparing API calls to [SocketCode]
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class AccountHistoryApiServiceImpl(
    private val socketCoreComponent: SocketCoreComponent,
    private val cryptoCoreComponent: CryptoCoreComponent
) : AccountHistoryApiService {

    override val api: Api = Api.ACCOUNT_HISTORY
}
