package org.echo.mobile.framework.model.operations

import org.echo.mobile.framework.exception.MalformedOperationException
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AccountOptions
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.eddsa.EdAuthority
import org.echo.mobile.framework.support.Builder

/**
 * Represents builder for [AccountCreateOperation].
 * Checks required data.
 *
 * @author Dmitriy Bushuev
 */
class AccountCreateOperationBuilder : Builder<AccountCreateOperation> {

    private var name: String? = null
    private var registrar: String? = null
    private var referrer: String? = null
    private var referrerPercent: Int = 0
    private var fee: AssetAmount? = null
    private var active: EdAuthority? = null
    private var edKey: String? = null
    private var options: AccountOptions? = null

    /**
     * Sets creating account name
     *
     * @param name Account for updating
     */
    fun setAccountName(name: String): AccountCreateOperationBuilder {
        this.name = name
        return this
    }

    /**
     * Sets registrar account id
     *
     * @param registrar Account for updating
     */
    fun setRegistrar(registrar: String): AccountCreateOperationBuilder {
        this.registrar = registrar
        return this
    }

    /**
     * Sets referrer account id
     *
     * @param referrer Account for updating
     */
    fun setReferrer(referrer: String): AccountCreateOperationBuilder {
        this.referrer = referrer
        return this
    }

    /**
     * Defines fee split between registrar and referrer,
     * this percentage goes to the referrer. The rest goes to the registrar.
     *
     * @param referrerPercent Account for updating
     */
    fun setReferrerPercent(referrerPercent: Int): AccountCreateOperationBuilder {
        this.referrerPercent = referrerPercent
        return this
    }

    /**
     * Sets actual fee [AssetAmount] for operation
     *
     * @param fee Actual fee [AssetAmount]
     */
    fun setFee(fee: AssetAmount): AccountCreateOperationBuilder {
        this.fee = fee
        return this
    }

    /**
     * Sets new active [EdAuthority] for account
     *
     * @param active New active [EdAuthority] for account
     */
    fun setActive(active: EdAuthority): AccountCreateOperationBuilder {
        this.active = active
        return this
    }

    /**
     * Sets new echorand ed key [edKey] for account
     */
    fun setEdKey(edKey: String): AccountCreateOperationBuilder {
        this.edKey = edKey
        return this
    }

    /**
     * Sets options for account
     *
     * @param newOptions New [AccountOptions] for account
     */
    fun setOptions(options: AccountOptions): AccountCreateOperationBuilder {
        this.options = options
        return this
    }

    override fun build(): AccountCreateOperation {
        checkName(name)
        checkConnectedAccounts(registrar, referrer)
        checkAuthoritiesAccountOptions(active, options)
        checkReferrerPercent()

        return fee?.let { nullSafeFee ->
            AccountCreateOperation(
                name!!,
                Account(registrar!!),
                Account(referrer!!),
                referrerPercent,
                active!!,
                edKey!!,
                options!!,
                nullSafeFee
            )
        } ?: AccountCreateOperation(
            name!!,
            Account(registrar!!),
            Account(referrer!!),
            referrerPercent,
            active!!,
            edKey!!,
            options!!
        )
    }

    private fun checkName(name: String?) {
        if (name == null)
            throw MalformedOperationException("Account create operation requires not null account name defined")
    }

    private fun checkConnectedAccounts(registrar: String?, referrer: String?) {
        if (registrar == null || referrer == null)
            throw MalformedOperationException("Account create operation requires not null registrar and referrer accounts id")
    }

    private fun checkReferrerPercent() {
        if (referrerPercent < 0)
            throw MalformedOperationException(
                "Account create operation requires not negative referrer percent value"
            )
    }

    private fun checkAuthoritiesAccountOptions(
        active: EdAuthority?,
        accountOptions: AccountOptions?
    ) {
        if (active == null && accountOptions == null) {
            throw MalformedOperationException(
                "Account create operation requires at least either an authority or account options change"
            )
        }
    }
}
