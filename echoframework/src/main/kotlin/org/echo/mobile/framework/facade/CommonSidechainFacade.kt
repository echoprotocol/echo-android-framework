package org.echo.mobile.framework.facade

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.Deposit
import org.echo.mobile.framework.model.SidechainType
import org.echo.mobile.framework.model.Withdraw

/**
 * Encapsulates logic, associated with common sidechain functionality
 *
 * @author Dmitriy Bushuev
 */
interface CommonSidechainFacade {

    /**
     * Retrieves list of account's [accountNameOrId] deposits [Deposit]
     */
    fun getAccountDeposits(
        accountNameOrId: String,
        sidechainType: SidechainType?,
        callback: Callback<List<Deposit?>>
    )

    /**
     * Retrieves list of account's [accountNameOrId] withdrawals [Withdraw]
     */
    fun getAccountWithdrawals(
        accountNameOrId: String,
        sidechainType: SidechainType?,
        callback: Callback<List<Withdraw?>>
    )

}