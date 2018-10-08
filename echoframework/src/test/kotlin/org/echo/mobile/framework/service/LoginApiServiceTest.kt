package org.echo.mobile.framework.service

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.service.internal.LoginApiServiceImpl
import org.echo.mobile.framework.support.Api
import org.junit.Assert.*
import org.junit.Test

/**
 * Test cases for [LoginApiServiceImpl]
 *
 * @author Daria Pechkovskaya
 */
class LoginApiServiceTest {

    @Test
    fun loginTest() {
        val response = true
        val socketCoreComponent = ServiceSocketCoreComponentMock(response)

        val loginApiService = LoginApiServiceImpl(socketCoreComponent)

        val loginCallback = object : Callback<Boolean> {
            override fun onSuccess(result: Boolean) {
                assertTrue(result)
            }

            override fun onError(error: LocalException) {
                fail()
            }
        }

        loginApiService.login(loginCallback)
    }

    @Test
    fun loginErrorTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(null)

        val loginApiService = LoginApiServiceImpl(socketCoreComponent)

        val loginCallback = object : Callback<Boolean> {
            override fun onSuccess(result: Boolean) {
                fail()
            }

            override fun onError(error: LocalException) {
                assertNotNull(error)
            }
        }

        loginApiService.login(loginCallback)
    }

    @Test
    fun connectApiTest() {
        val response = 3
        val apiToConnect = Api.DATABASE
        val socketCoreComponent = ServiceSocketCoreComponentMock(response)

        val loginApiService = LoginApiServiceImpl(socketCoreComponent)

        val connectApiCallback = object : Callback<Int> {
            override fun onSuccess(result: Int) {
                assertEquals(result, response)
            }

            override fun onError(error: LocalException) {
                fail()
            }
        }

        loginApiService.connectApi(apiToConnect, connectApiCallback)
    }

    @Test
    fun connectApiErrorTest() {
        val apiToConnect = Api.DATABASE
        val socketCoreComponent = ServiceSocketCoreComponentMock(null)

        val loginApiService = LoginApiServiceImpl(socketCoreComponent)

        val connectApiCallback = object : Callback<Int> {
            override fun onSuccess(result: Int) {
                fail()
            }

            override fun onError(error: LocalException) {
                assertNotNull(error)
            }
        }

        loginApiService.connectApi(apiToConnect, connectApiCallback)
    }

}