package org.echo.mobile.framework

import org.echo.mobile.framework.facade.AssetsFacade
import org.echo.mobile.framework.facade.AuthenticationFacade
import org.echo.mobile.framework.facade.ContractsFacade
import org.echo.mobile.framework.facade.FeeFacade
import org.echo.mobile.framework.facade.InformationFacade
import org.echo.mobile.framework.facade.SubscriptionFacade
import org.echo.mobile.framework.facade.TransactionsFacade
import org.echo.mobile.framework.service.AccountHistoryApiService
import org.echo.mobile.framework.service.CryptoApiService
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.service.LoginApiService
import org.echo.mobile.framework.service.NetworkBroadcastApiService
import org.echo.mobile.framework.service.RegistrationApiService
import org.echo.mobile.framework.support.Settings

/**
 * Describes base library functionality
 *
 * Represents base library facade,
 * that combines all other facades to provide the only entry point of library
 *
 * @author Dmitriy Bushuev
 */
interface EchoFramework : AuthenticationFacade, FeeFacade, InformationFacade, SubscriptionFacade,
    TransactionsFacade, AssetsFacade, ContractsFacade {

    /**
     * Current library instance of [AccountHistoryApiService]
     */
    val accountHistoryApiService: AccountHistoryApiService

    /**
     * Current library instance of [DatabaseApiService]
     */
    val databaseApiService: DatabaseApiService

    /**
     * Current library instance of [NetworkBroadcastApiService]
     */
    val networkBroadcastApiService: NetworkBroadcastApiService

    /**
     * Current library instance of [CryptoApiService]
     */
    val cryptoApiService: CryptoApiService

    /**
     * Current library instance of [LoginApiService]
     */
    val loginService: LoginApiService

    /**
     * Current library instance of [RegistrationApiService]
     */
    val registrationService: RegistrationApiService

    /**
     * Starts socket connection, connects to blockchain apis
     */
    fun start(callback: Callback<Any>)

    /**
     * Stops socket connection, unsubscribe all listeners
     */
    fun stop()

    companion object {

        /**
         * Creates library with settings
         * @param settings Settings for initialization
         */
        fun create(settings: Settings): EchoFramework = EchoFrameworkImpl(settings)
    }

}
