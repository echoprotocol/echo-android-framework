package org.echo.mobile.framework.service

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.support.Api

/**
 * Encapsulates logic, associated with blockchain login API
 *
 * [Login API details](https://dev-doc.myecho.app/classgraphene_1_1app_1_1login__api.html)
 *
 * @author Daria Pechkovskaya
 */
interface LoginApiService : ApiService, CustomOperationService {

    /**
     * Authenticate to the RPC server.
     *
     * @param callback Listener of call result. True if connected successful.
     */
    fun login(callback: Callback<Boolean>)

    /**
     * Retrieve the API id from blockchain.
     *
     * @param api Api to retrieving
     * @param callback Listener of call result. Retrieves id of api
     */
    fun connectApi(api: Api, callback: Callback<Int>)

    companion object {
        /**
         * Blockchain api id for initializing another apis
         */
        const val INITIALIZER_API_ID = 1
    }

}
