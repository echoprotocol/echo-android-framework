package com.pixelplex.echolib

import com.pixelplex.echolib.facade.*

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
interface EchoLib : AuthenticationFacade, FeeFacade, InformationFacade, SubscriptionFacade,
    TransactionsFacade
