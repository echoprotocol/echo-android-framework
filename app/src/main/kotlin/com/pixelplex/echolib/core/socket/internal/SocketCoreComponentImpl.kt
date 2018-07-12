package com.pixelplex.echolib.core.socket.internal

import com.pixelplex.echolib.core.socket.SocketCoreComponent
import com.pixelplex.echolib.core.socket.SocketMessenger
import com.pixelplex.echolib.support.model.Api

/**
 * Implementation of [SocketCoreComponent]
 *
 * @author Daria Pechkovskaya
 */
class SocketCoreComponentImpl(socketMessenger: SocketMessenger, apis: Set<Api>) :
    SocketCoreComponent
