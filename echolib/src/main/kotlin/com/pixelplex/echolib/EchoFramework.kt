package com.pixelplex.echolib

import com.pixelplex.echolib.facade.*
import com.pixelplex.echolib.support.Settings

/**
 * Describes base library functionality
 *
 * <p>
 *     Represents base library facade,
 *     that combines all other facades to provide the only entry point of library
 * </p>
 *
 * @author Dmitriy Bushuev
 */
interface EchoFramework : AuthenticationFacade, FeeFacade, InformationFacade, SubscriptionFacade,
    TransactionsFacade {

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
