package com.pixelplex.echoframework.service.internal

import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.core.socket.SocketCoreComponent
import com.pixelplex.echoframework.model.socketoperations.AccessSocketOperation
import com.pixelplex.echoframework.model.socketoperations.AccessSocketOperationType
import com.pixelplex.echoframework.model.socketoperations.LoginSocketOperation
import com.pixelplex.echoframework.service.LoginApiService
import com.pixelplex.echoframework.support.Api
import com.pixelplex.echoframework.support.Converter

/**
 * Implementation of [LoginApiService]
 *
 * @author Daria Pechkovskaya
 */
class LoginApiServiceImpl(val socketCoreComponent: SocketCoreComponent) : LoginApiService {

    override var id: Int = LoginApiService.INITIALIZER_API_ID

    override fun login(callback: Callback<Boolean>) {
        val loginOperation = LoginSocketOperation(socketCoreComponent.currentId, id, callback)
        socketCoreComponent.emit(loginOperation)
    }

    override fun connectApi(api: Api, callback: Callback<Int>) {
        val apiTypeConverter = ApiToOperationTypeConverter()
        val operation = AccessSocketOperation(
            accessSocketType = apiTypeConverter.convert(api),
            api = id,
            callId = socketCoreComponent.currentId,
            callback = callback
        )
        socketCoreComponent.emit(operation)
    }

    private class ApiToOperationTypeConverter : Converter<Api, AccessSocketOperationType> {

        private val apiToType = hashMapOf(
            Api.DATABASE to AccessSocketOperationType.DATABASE,
            Api.NETWORK_BROADCAST to AccessSocketOperationType.NETWORK_BROADCAST,
            Api.ACCOUNT_HISTORY to AccessSocketOperationType.HISTORY,
            Api.CRYPTO to AccessSocketOperationType.CRYPTO
        )

        override fun convert(source: Api): AccessSocketOperationType =
            apiToType[source] ?: throw IllegalArgumentException("Unrecognized api type: $source")
    }
}
