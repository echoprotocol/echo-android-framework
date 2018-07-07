package com.pixelplex.echolib.service.internal

import com.pixelplex.echolib.core.CryptoCore
import com.pixelplex.echolib.core.SocketCore
import com.pixelplex.echolib.service.AccountHistoryApiService

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
    private val socketCore: SocketCore,
    private val cryptoCore: CryptoCore
) : AccountHistoryApiService
