package com.pixelplex.echoframework.model.operations

import com.pixelplex.echoframework.exception.MalformedOperationException
import com.pixelplex.echoframework.model.Account
import com.pixelplex.echoframework.model.AssetAmount
import com.pixelplex.echoframework.model.Memo
import com.pixelplex.echoframework.support.Builder

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
    private var transferMemo: Memo? = null

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

    /**
     * Defines fee value of issue operation
     */
    fun setMemo(memo: Memo): IssueAssetOperationBuilder {
        this.transferMemo = memo
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
        }.apply { memo = transferMemo ?: Memo() }
    }

}
