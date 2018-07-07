package com.pixelplex.echolib.service.internal

import com.pixelplex.echolib.core.SocketCore
import com.pixelplex.echolib.service.DatabaseApiService

/**
 * Implementation of [DatabaseApiService]
 *
 * <p>
 *     Encapsulates logic of preparing API calls to [SocketCode]
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class DatabaseApiServiceImpl(private val socketCore: SocketCore) : DatabaseApiService
