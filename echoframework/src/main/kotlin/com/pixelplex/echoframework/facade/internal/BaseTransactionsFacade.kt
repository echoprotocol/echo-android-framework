package com.pixelplex.echoframework.facade.internal

import com.pixelplex.echoframework.ECHO_ASSET_ID
import com.pixelplex.echoframework.core.crypto.CryptoCoreComponent
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.model.*
import com.pixelplex.echoframework.service.DatabaseApiService
import com.pixelplex.echoframework.support.dematerialize
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
