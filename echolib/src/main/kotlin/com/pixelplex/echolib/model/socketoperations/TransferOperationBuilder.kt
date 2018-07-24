package com.pixelplex.echolib.model.socketoperations

import com.pixelplex.echolib.exception.MalformedOperationException
import com.pixelplex.echolib.model.Account
import com.pixelplex.echolib.model.AssetAmount
import com.pixelplex.echolib.support.operationbuilders.OperationBuilder

/**
 * Builder class for transfer operation [TransferOperation]
 *
 * @author Dmitriy Bushuev
 */
class TransferOperationBuilder : OperationBuilder<TransferOperation>() {
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
        val transferOperation: TransferOperation = if (fee != null) {
            TransferOperation(from!!, to!!, transferAmount!!, fee!!)
        } else {
            TransferOperation(from!!, to!!, transferAmount!!)
        }

        when {
            from == null -> throw MalformedOperationException("Missing source account information")
            to == null -> throw MalformedOperationException("Missing destination account information")
            transferAmount == null -> throw MalformedOperationException("Missing transfer amount information")
            else -> return transferOperation
        }

    }
}
