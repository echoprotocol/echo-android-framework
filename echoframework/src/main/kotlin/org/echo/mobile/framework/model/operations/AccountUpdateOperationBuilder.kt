package org.echo.mobile.framework.model.operations

import org.echo.mobile.framework.exception.MalformedOperationException
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AccountOptions
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.Authority
import org.echo.mobile.framework.support.Builder

/**
 * Represents builder for [AccountUpdateOperation].
 * Checks required data.
 *
 * @author Daria Pechkovskaya
 */
class AccountUpdateOperationBuilder : Builder<AccountUpdateOperation> {

    private var fee: AssetAmount? = null
    private var account: Account? = null
    private var active: Authority? = null
    private var edKey: String? = null
    private var newOptions: AccountOptions? = null

    /**
     * Sets actual fee [AssetAmount] for operation
     * @param fee Actual fee [AssetAmount]
     */
    fun setFee(fee: AssetAmount): AccountUpdateOperationBuilder {
        this.fee = fee
        return this
    }

    /**
     * Sets account for updating
     * @param account Account for updating
     */
    fun setAccount(account: Account): AccountUpdateOperationBuilder {
        this.account = account
        return this
    }

    /**
     * Sets new active [Authority] for account
     * @param active New active [Authority] for account
     */
    fun setActive(active: Authority): AccountUpdateOperationBuilder {
        this.active = active
        return this
    }

    /**
     * Sets new echorand ed key [edKey] for account
     */
    fun setEdKey(edKey: String): AccountUpdateOperationBuilder {
        this.edKey = edKey
        return this
    }

    /**
     * Sets new options for account
     * @param newOptions New [AccountOptions] for account
     */
    fun setOptions(newOptions: AccountOptions): AccountUpdateOperationBuilder {
        this.newOptions = newOptions
        return this
    }

    override fun build(): AccountUpdateOperation {
        checkAccount(account)
        checkAuthoritiesAccountOptions(active, newOptions)

        return fee?.let { nullSafeFee ->
            AccountUpdateOperation(account!!, active, edKey, newOptions, nullSafeFee)
        } ?: AccountUpdateOperation(account!!, active, edKey, newOptions)
    }

    private fun checkAccount(account: Account?) {
        if (account == null)
            throw MalformedOperationException("This operation requires an account to be set")
    }

    private fun checkAuthoritiesAccountOptions(
        active: Authority?,
        accountOptions: AccountOptions?
    ) {
        if (active == null && accountOptions == null) {
            throw MalformedOperationException("This operation requires at least either an authority or account options change")
        }
    }
}
