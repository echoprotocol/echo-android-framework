package org.echo.mobile.framework.model.operations

import org.echo.mobile.framework.exception.MalformedOperationException
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.support.Builder

/**
 * Represents builder for [BalanceFreezeOperation].
 * Checks required data.
 *
 * @author Pavel Savchkov
 */
class BalanceFreezeOperationBuilder : Builder<BalanceFreezeOperation> {

    private var fee: AssetAmount? = null
    private var account: Account? = null
    private var amount: AssetAmount? = null
    private var duration: Int? = null

    /**
     * Sets actual fee [AssetAmount] for operation
     *
     * @param fee Actual fee [AssetAmount]
     */
    fun setFee(fee: AssetAmount): BalanceFreezeOperationBuilder {
        this.fee = fee
        return this
    }

    /**
     * Sets the target account for the operation for account
     *
     * @param account New account [Account] for operation
     */
    fun setAccount(account: Account): BalanceFreezeOperationBuilder {
        this.account = account
        return this
    }

    /**
     * Sets amount [AssetAmount] for operation
     *
     * @param amount total claimed amount [AssetAmount]
     */
    fun setAmount(amount: AssetAmount): BalanceFreezeOperationBuilder {
        this.amount = amount
        return this
    }

    /**
     * Sets duration [duration]
     */
    fun setDuration(duration: Int): BalanceFreezeOperationBuilder {
        this.duration = duration
        return this
    }

    override fun build(): BalanceFreezeOperation {
        checkAccount(account)
        checkAmount(amount)
        checkDuration(duration)

        return fee?.let { nullSafeFee ->
            BalanceFreezeOperation(
                    account!!,
                    amount!!,
                    duration!!,
                    nullSafeFee
            )
        } ?: BalanceFreezeOperation(
                account!!,
                amount!!,
                duration!!
        )
    }

    private fun checkAccount(account: Account?) {
        if (account == null)
            throw MalformedOperationException("Balance freeze operation requires not null account defined")
    }

    private fun checkAmount(totalClaimed: AssetAmount?) {
        if (totalClaimed == null) {
            throw MalformedOperationException(
                    "Balance freeze operation requires amount"
            )
        }
    }

    private fun checkDuration(duration: Int?) {
        if (duration == null)
            throw MalformedOperationException("Balance freeze operation requires not null duration")
    }
}
