package org.echo.mobile.framework.service

import org.echo.mobile.framework.model.contract.ContractLog

/**
 * Encapsulates logic connected with getting contract logs with callback subscription
 *
 * @author Daria Pechkovskaya
 */
interface ContractLogsSubscriptionManager : NotifiedSubscriptionManager<List<ContractLog>>
