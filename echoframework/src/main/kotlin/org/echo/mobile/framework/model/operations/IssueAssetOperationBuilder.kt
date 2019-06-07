package org.echo.mobile.framework.model.operations

import org.echo.mobile.framework.exception.MalformedOperationException
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.Memo
import org.echo.mobile.framework.support.Builder

/**
 * Builder class for asset issue operation [IssueAssetOperation]
 *
 * @author Dmitriy Bushuev
 */
class IssueAssetOperationBuilder : Builder<IssueAssetOperation> {
    private var issuer: Account? = null
    private var destination: Account? = null
    private var issueAmount: AssetAmount? = null
    private var fee: AssetAmount? = null

    /**
     * Defines source account of issue operation
     */
    fun setIssuer(issuer: Account): IssueAssetOperationBuilder {
        this.issuer = issuer
        return this
    }

    /**
     * Defines target account of issue operation
     */
    fun setDestination(destination: Account): IssueAssetOperationBuilder {
        this.destination = destination
        return this
    }

    /**
     * Defines amount value of issue operation
     */
    fun setAmount(issueAmount: AssetAmount): IssueAssetOperationBuilder {
        this.issueAmount = issueAmount
        return this
    }

    /**
     * Defines fee value of issue operation
     */
    fun setFee(fee: AssetAmount): IssueAssetOperationBuilder {
        this.fee = fee
        return this
    }

    override fun build(): IssueAssetOperation {
        when {
            issuer == null -> throw MalformedOperationException("Missing source account information")
            destination == null -> throw MalformedOperationException("Missing destination account information")
            issueAmount == null -> throw MalformedOperationException("Missing issue amount information")
        }

        return if (fee != null) {
            IssueAssetOperation(issuer!!, issueAmount!!, destination!!, fee!!)
        } else {
            IssueAssetOperation(issuer!!, issueAmount!!, destination!!)
        }
    }

}
