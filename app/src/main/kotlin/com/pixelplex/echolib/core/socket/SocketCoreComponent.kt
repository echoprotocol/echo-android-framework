package com.pixelplex.echolib.core.socket

import com.pixelplex.echolib.model.socketoperations.SocketOperation

/**
 * Encapsulates logic, associated with socket API calls to Graphene blockchain
 *
 * @author Dmitriy Bushuev
 */
interface SocketCoreComponent {

    val socketState: SocketState

    /**
     * Send operation to blockchain
     * @param operation Operation to send to blockchain
     */
    fun send(operation: SocketOperation)

}
