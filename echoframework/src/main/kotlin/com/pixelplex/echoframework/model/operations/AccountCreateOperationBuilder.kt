package com.pixelplex.echoframework.model.operations

import com.pixelplex.echoframework.exception.MalformedOperationException
import com.pixelplex.echoframework.model.AccountOptions
import com.pixelplex.echoframework.model.AssetAmount
import com.pixelplex.echoframework.model.Authority
import com.pixelplex.echoframework.support.Builder

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
    private var owner: Authority? = null
    private var active: Authority? = null
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
     * Sets new owner [Authority] for account
     *
     * @param owner New owner [Authority] for account
     */
    fun setOwner(owner: Authority): AccountCreateOperationBuilder {
        this.owner = owner
        return this
    }

    /**
     * Sets new active [Authority] for account
     *
     * @param active New active [Authority] for account
     */
    fun setActive(active: Authority): AccountCreateOperationBuilder {
        this.active = active
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
        checkAuthoritiesAccountOptions(owner, active, options)
        checkReferrerPercent()

        return fee?.let { nullSafeFee ->
            AccountCreateOperation(
                name!!,
                registrar!!,
                referrer!!,
                referrerPercent,
                owner!!,
                active!!,
                options!!,
                nullSafeFee
            )
        } ?: AccountCreateOperation(
            name!!,
            registrar!!,
            referrer!!,
            referrerPercent,
            owner!!,
            active!!,
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
        owner: Authority?,
        active: Authority?,
        accountOptions: AccountOptions?
    ) {
        if (owner == null && active == null && accountOptions == null) {
            throw MalformedOperationException(
                "Account create operation requires at least either an authority or account options change"
            )
        }
    }
}
