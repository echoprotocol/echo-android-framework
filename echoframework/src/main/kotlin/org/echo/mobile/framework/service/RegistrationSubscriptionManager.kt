package org.echo.mobile.framework.service

import org.echo.mobile.framework.model.RegistrationResult

/**
 * Encapsulates logic connected with registration with callback subscription
 *
 * @author Daria Pechkovskaya
 */
interface RegistrationSubscriptionManager: NotifiedSubscriptionManager<RegistrationResult>
