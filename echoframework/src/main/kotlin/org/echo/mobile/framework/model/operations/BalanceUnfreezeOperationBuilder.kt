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
class BalanceUnfreezeOperationBuilder : Builder<BalanceUnfreezeOperation> {

    private var fee: AssetAmount? = null
    private var account: Account? = null
    private var amount: AssetAmount? = null

    /**
     * Sets actual fee [AssetAmount] for operation
     *
     * @param fee Actual fee [AssetAmount]
     */
    fun setFee(fee: AssetAmount): BalanceUnfreezeOperationBuilder {
        this.fee = fee
        return this
    }

    /**
     * Sets the target account for the operation for account
     *
     * @param account New account [Account] for operation
     */
    fun setAccount(account: Account): BalanceUnfreezeOperationBuilder {
        this.account = account
        return this
    }

    /**
     * Sets amount [AssetAmount] for operation
     *
     * @param amount total claimed amount [AssetAmount]
     */
    fun setAmount(amount: AssetAmount): BalanceUnfreezeOperationBuilder {
        this.amount = amount
        return this
    }

    override fun build(): BalanceUnfreezeOperation {
        checkAccount(account)
        checkAmount(amount)

        return fee?.let { nullSafeFee ->
            BalanceUnfreezeOperation(
                    account!!,
                    amount!!,
                    nullSafeFee
            )
        } ?: BalanceUnfreezeOperation(
                account!!,
                amount!!
        )
    }

    private fun checkAccount(account: Account?) {
        if (account == null)
            throw MalformedOperationException("Balance unfreeze operation requires not null account defined")
    }

    private fun checkAmount(totalClaimed: AssetAmount?) {
        if (totalClaimed == null) {
            throw MalformedOperationException(
                    "Balance unfreeze operation requires amount"
            )
        }
    }
}
