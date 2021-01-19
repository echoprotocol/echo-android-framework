package org.echo.mobile.framework.model.operations

import org.echo.mobile.framework.exception.MalformedOperationException
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.support.Builder

/**
 * Represents builder for [BalanceClaimOperation].
 * Checks required data.
 *
 * @author Pavel Savchkov
 */
class BalanceClaimOperationBuilder : Builder<BalanceClaimOperation> {

    private var fee: AssetAmount? = null
    private var depositToAccount: Account? = null
    private var balanceToClaimId: String? = null
    private var balanceOwnerKey: String? = null
    private var totalClaimed: AssetAmount? = null

    /**
     * Sets actual fee [AssetAmount] for operation
     *
     * @param fee Actual fee [AssetAmount]
     */
    fun setFee(fee: AssetAmount): BalanceClaimOperationBuilder {
        this.fee = fee
        return this
    }

    /**
     * Sets the target account for the operation for account
     *
     * @param depositToAccount New account [Account] for operation
     */
    fun setDepositToAccount(depositToAccount: Account): BalanceClaimOperationBuilder {
        this.depositToAccount = depositToAccount
        return this
    }

    /**
     * Sets new id of balance to claim [balanceToClaimId]
     */
    fun setBalanceToClaimId(balanceToClaimId: String): BalanceClaimOperationBuilder {
        this.balanceToClaimId = balanceToClaimId
        return this
    }

    /**
     * Sets new key of the balance owner [balanceOwnerKey]
     */
    fun setBalanceOwnerKey(balanceOwnerKey: String): BalanceClaimOperationBuilder {
        this.balanceOwnerKey = balanceOwnerKey
        return this
    }

    /**
     * Sets total claimed amount [AssetAmount] for operation
     *
     * @param totalClaimed total claimed amount [AssetAmount]
     */
    fun setTotalClaimed(totalClaimed: AssetAmount): BalanceClaimOperationBuilder {
        this.totalClaimed = totalClaimed
        return this
    }

    override fun build(): BalanceClaimOperation {
        checkAccount(depositToAccount)
        checkBalance(balanceToClaimId, balanceOwnerKey)
        checkAmount(totalClaimed)

        return fee?.let { nullSafeFee ->
            BalanceClaimOperation(
                    depositToAccount!!,
                    balanceToClaimId!!,
                    balanceOwnerKey!!,
                    totalClaimed!!,
                    nullSafeFee
            )
        } ?:BalanceClaimOperation(
                depositToAccount!!,
                balanceToClaimId!!,
                balanceOwnerKey!!,
                totalClaimed!!
        )
    }

    private fun checkAccount(account: Account?) {
        if (account == null)
            throw MalformedOperationException("Balance claim operation requires not null deposit to account defined")
    }

    private fun checkBalance(balanceToClaimId: String?, balanceOwnerKey: String?) {
        if (balanceToClaimId == null || balanceOwnerKey == null)
            throw MalformedOperationException("Balance claim operation requires not null balance id and owner okey")
    }

    private fun checkAmount(totalClaimed: AssetAmount?) {
        if (totalClaimed == null) {
            throw MalformedOperationException(
                    "Balance claim operation requires total claimed amount"
            )
        }
    }
}
