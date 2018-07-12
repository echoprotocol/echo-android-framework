package com.pixelplex.echolib

import com.pixelplex.echolib.facade.*
import com.pixelplex.echolib.support.model.Settings

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

    companion object {

        /**
         * Initialize library with settings
         * @param settings Settings for initialization
         */
        fun initialize(settings: Settings): EchoFramework = EchoFrameworkImpl(settings)
    }
}
