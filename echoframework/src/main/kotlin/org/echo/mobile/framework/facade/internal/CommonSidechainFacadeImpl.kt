package org.echo.mobile.framework.facade.internal

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.exception.AccountNotFoundException
import org.echo.mobile.framework.facade.CommonSidechainFacade
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.Deposit
import org.echo.mobile.framework.model.SidechainType
import org.echo.mobile.framework.model.Withdraw
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.support.dematerialize

/**
 * Implementation of [CommonSidechainFacade]
 *
 * Wraps base logic for sidechain types processing
 *
 * @author Dmitriy Bushuev
 */
class CommonSidechainFacadeImpl(
    private val databaseApiService: DatabaseApiService
) : CommonSidechainFacade {

    override fun getAccountDeposits(
        accountNameOrId: String,
        sidechainType: SidechainType?,
        callback: Callback<List<Deposit?>>
    ) {
        val account = findAccount(accountNameOrId)

        databaseApiService.getAccountDeposits(account.getObjectId(), sidechainType, callback)
    }

    override fun getAccountWithdrawals(
        accountNameOrId: String,
        sidechainType: SidechainType?,
        callback: Callback<List<Withdraw?>>
    ) {
        val account = findAccount(accountNameOrId)

        databaseApiService.getAccountWithdrawals(account.getObjectId(), sidechainType, callback)
    }

    private fun findAccount(nameOrId: String): Account {
        val accountsMap =
            databaseApiService.getFullAccounts(listOf(nameOrId), false).dematerialize()
        return accountsMap[nameOrId]?.account
            ?: throw AccountNotFoundException("Unable to find required account $nameOrId")
    }

}