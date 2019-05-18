package org.echo.mobile.framework.service

import org.echo.mobile.framework.model.TransactionResult

/**
 * Encapsulates logic connected with broadcast transaction with callback subscription
 *
 * @author Daria Pechkovskaya
 */
interface TransactionSubscriptionManager : NotifiedSubscriptionManager<TransactionResult>
