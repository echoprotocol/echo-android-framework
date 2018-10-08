package org.echo.mobile.framework.service.internal

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.socket.SocketCoreComponent
import org.echo.mobile.framework.model.socketoperations.AccessSocketOperation
import org.echo.mobile.framework.model.socketoperations.AccessSocketOperationType
import org.echo.mobile.framework.model.socketoperations.LoginSocketOperation
import org.echo.mobile.framework.service.LoginApiService
import org.echo.mobile.framework.support.Api
import org.echo.mobile.framework.support.Converter

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
