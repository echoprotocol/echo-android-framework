package org.echo.mobile.framework.facade.internal

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.facade.TransactionsFacade
import org.echo.mobile.framework.model.*
import org.echo.mobile.framework.model.operations.TransferOperationBuilder
import org.echo.mobile.framework.processResult
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.service.NetworkBroadcastApiService
import org.echo.mobile.framework.support.dematerialize
import org.echo.mobile.framework.support.error
import org.echo.mobile.framework.support.value

/**
 * Implementation of [TransactionsFacade]
 *
 * Delegates API call logic to [DatabaseApiService] and [NetworkBroadcastApiService]
 *
 * @author Dmitriy Bushuev
 */
class TransactionsFacadeImpl(
    private val databaseApiService: DatabaseApiService,
    private val networkBroadcastApiService: NetworkBroadcastApiService,
    private val cryptoCoreComponent: CryptoCoreComponent
) : BaseTransactionsFacade(databaseApiService, cryptoCoreComponent), TransactionsFacade {

    override fun sendTransferOperation(
        nameOrId: String,
        password: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        feeAsset: String?,
        message: String?,
        callback: Callback<Boolean>
    ) = callback.processResult {
        var toAccount: Account? = null
        var fromAccount: Account? = null

        databaseApiService.getFullAccounts(listOf(nameOrId, toNameOrId), false)
            .value { accountsMap ->
                fromAccount = accountsMap[nameOrId]?.account
                        ?: throw LocalException("Unable to find required account $nameOrId")
                toAccount = accountsMap[toNameOrId]?.account
                        ?: throw LocalException("Unable to find required account $toNameOrId")
            }
            .error { accountsError ->
                throw LocalException("Error occurred during accounts request", accountsError)
            }

        checkOwnerAccount(fromAccount!!.name, password, fromAccount!!)

        val privateKey =
            cryptoCoreComponent.getPrivateKey(
                fromAccount!!.name,
                password,
                AuthorityType.ACTIVE
            )

        val memoPrivateKey = memoKey(fromAccount!!.name, password)
        val memo = generateMemo(memoPrivateKey, fromAccount!!, toAccount!!, message)

        val transfer = TransferOperationBuilder()
            .setFrom(fromAccount!!)
            .setTo(toAccount!!)
            .setAmount(AssetAmount(UnsignedLong.valueOf(amount.toLong()), Asset(asset)))
            .setMemo(memo)
            .build()

        val blockData = databaseApiService.getBlockData()
        val chainId = getChainId()
        val fees = getFees(listOf(transfer), feeAsset ?: asset)

        val transaction = Transaction(
            blockData,
            listOf(transfer),
            chainId
        ).apply {
            setFees(fees)
            addPrivateKey(privateKey)
        }

        networkBroadcastApiService.broadcastTransactionWithCallback(transaction).dematerialize()
    }

}
