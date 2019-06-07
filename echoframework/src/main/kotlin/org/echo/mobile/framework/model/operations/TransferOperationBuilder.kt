package org.echo.mobile.framework.model.operations

import org.echo.mobile.framework.exception.MalformedOperationException
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.Memo
import org.echo.mobile.framework.support.Builder

/**
 * Builder class for transfer operation [TransferOperation]
 *
 * @author Dmitriy Bushuev
 */
class TransferOperationBuilder : Builder<TransferOperation> {
    private var from: Account? = null
    private var to: Account? = null
    private var transferAmount: AssetAmount? = null
    private var fee: AssetAmount? = null

    /**
     * Defines source account of transfer operation
     */
    fun setFrom(from: Account): TransferOperationBuilder {
        this.from = from
        return this
    }

    /**
     * Defines target account of transfer operation
     */
    fun setTo(to: Account): TransferOperationBuilder {
        this.to = to
        return this
    }

    /**
     * Defines amount value of transfer operation
     */
    fun setAmount(transferAmount: AssetAmount): TransferOperationBuilder {
        this.transferAmount = transferAmount
        return this
    }

    /**
     * Defines fee value of transfer operation
     */
    fun setFee(fee: AssetAmount): TransferOperationBuilder {
        this.fee = fee
        return this
    }

    override fun build(): TransferOperation {
        when {
            from == null -> throw MalformedOperationException("Missing source account information")
            to == null -> throw MalformedOperationException("Missing destination account information")
            transferAmount == null -> throw MalformedOperationException("Missing transfer amount information")
        }

        return if (fee != null) {
            TransferOperation(from!!, to!!, transferAmount!!, fee!!)
        } else {
            TransferOperation(from!!, to!!, transferAmount!!)
        }
    }
}
