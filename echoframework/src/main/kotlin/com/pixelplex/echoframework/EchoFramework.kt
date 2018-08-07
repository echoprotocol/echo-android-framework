package com.pixelplex.echoframework

import com.pixelplex.echoframework.facade.*
import com.pixelplex.echoframework.support.Settings

/**
 * Describes base library functionality
 *
 * Represents base library facade,
 * that combines all other facades to provide the only entry point of library
 *
 * @author Dmitriy Bushuev
 */
interface EchoFramework : AuthenticationFacade, FeeFacade, InformationFacade, SubscriptionFacade,
    TransactionsFacade, ContractsFacade {

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
