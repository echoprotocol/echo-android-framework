package org.echo.mobile.framework.service.internal.subscription

import org.echo.mobile.framework.model.BaseOperation
import org.echo.mobile.framework.model.Block
import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.model.operations.ContractOperation
import org.echo.mobile.framework.model.operations.OperationType
import org.echo.mobile.framework.service.ContractSubscriptionManager
import org.echo.mobile.framework.service.UpdateListener

/**
 * Implementation of [UpdateListener] for [Block] data,
 * that encapsulates logic of block transactions parsing and notifying contract listeners,
 * if transactions contains operations with contract
 *
 * @author Dmitriy Bushuev
 */
class ContractBlockListenerDelegate(private val contractSubscriptionManager: ContractSubscriptionManager) :
    UpdateListener<Block> {

    override fun onUpdate(data: Block) {
        val transactions = data.transactions

        transactions.forEach { transaction ->
            processTransaction(transaction)
        }
    }

    private fun processTransaction(transaction: Transaction) {
        transaction.operations.forEach { operation ->
            processOperation(operation)
        }
    }

    private fun processOperation(operation: BaseOperation) {
        if (operation.type == OperationType.CONTRACT_OPERATION) {
            notify(operation as ContractOperation)
        }
    }

    private fun notify(contractOperation: ContractOperation) =
        contractOperation.receiver.field?.let {
            contractSubscriptionManager.notify(it)
        }

}
