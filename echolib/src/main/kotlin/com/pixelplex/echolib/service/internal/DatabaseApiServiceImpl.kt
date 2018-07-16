package com.pixelplex.echolib.service.internal

import com.pixelplex.echolib.core.socket.SocketCoreComponent
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
class DatabaseApiServiceImpl(private val socketCoreComponent: SocketCoreComponent) : DatabaseApiService
