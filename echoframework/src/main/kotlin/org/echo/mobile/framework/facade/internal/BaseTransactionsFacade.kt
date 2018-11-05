package org.echo.mobile.framework.facade.internal

import org.echo.mobile.framework.ECHO_ASSET_ID
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.core.socket.SocketCoreComponent
import org.echo.mobile.framework.core.socket.SocketMessengerListener
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.*
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.service.TransactionSubscriptionManager
import org.echo.mobile.framework.service.internal.subscription.TransactionSubscriptionManagerImpl
import org.echo.mobile.framework.support.Result
import org.echo.mobile.framework.support.concurrent.future.FutureTask
import org.echo.mobile.framework.support.concurrent.future.completeCallback
import org.echo.mobile.framework.support.concurrent.future.wrapResult
import org.echo.mobile.framework.support.dematerialize
import org.echo.mobile.framework.support.toJsonObject
import java.math.BigInteger

/**
 * Includes base logic for transactions assembly
 *
 * @author Daria Pechkovskaya
 */
abstract class BaseTransactionsFacade(
    private val databaseApiService: DatabaseApiService,
    private val cryptoCoreComponent: CryptoCoreComponent
) {

    protected fun getChainId(): String = databaseApiService.getChainId().dematerialize()

    protected fun getFees(
        operations: List<BaseOperation>,
        asset: Asset = Asset(ECHO_ASSET_ID)
    ): List<AssetAmount> = databaseApiService.getRequiredFees(operations, asset).dematerialize()

    protected fun getFees(operations: List<BaseOperation>, assetId: String): List<AssetAmount> =
        databaseApiService.getRequiredFees(operations, Asset(assetId)).dematerialize()

    protected fun checkOwnerAccount(name: String, password: String, account: Account) {
        val ownerAddress =
            cryptoCoreComponent.getAddress(name, password, AuthorityType.OWNER)

        val isKeySame = account.isEqualsByKey(ownerAddress, AuthorityType.OWNER)
        if (!isKeySame) {
            throw LocalException("Owner account checking exception")
        }
    }

    protected fun memoKey(name: String, password: String) =
        cryptoCoreComponent.getPrivateKey(name, password, AuthorityType.KEY)

    protected fun generateMemo(
        privateKey: ByteArray,
        fromAccount: Account,
        toAccount: Account,
        message: String?
    ): Memo {
        if (message != null) {
            val encryptedMessage = cryptoCoreComponent.encryptMessage(
                privateKey,
                toAccount.options.memoKey!!.key,
                BigInteger.ZERO,
                message
            )

            return Memo(
                Address(fromAccount.options.memoKey!!),
                Address(toAccount.options.memoKey!!),
                BigInteger.ZERO,
                encryptedMessage ?: ByteArray(0)
            )
        }

        return Memo()
    }

}
