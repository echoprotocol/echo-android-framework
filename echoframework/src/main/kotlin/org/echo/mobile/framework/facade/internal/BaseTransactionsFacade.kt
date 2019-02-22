package org.echo.mobile.framework.facade.internal

import org.echo.mobile.framework.ECHO_ASSET_ID
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.Address
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.AuthorityType
import org.echo.mobile.framework.model.BaseOperation
import org.echo.mobile.framework.model.Memo
import org.echo.mobile.framework.model.isEqualsByKey
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.support.dematerialize
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
            cryptoCoreComponent.getAddress(name, password, AuthorityType.ACTIVE)

        val isKeySame = account.isEqualsByKey(ownerAddress, AuthorityType.ACTIVE)
        if (!isKeySame) {
            throw LocalException("Owner account checking exception")
        }
    }

    protected fun checkOwnerAccount(wif: String, account: Account) {
        val privateKey = cryptoCoreComponent.decodeFromWif(wif)
        val publicKey = cryptoCoreComponent.derivePublicKeyFromPrivate(privateKey)
        val address = cryptoCoreComponent.getAddressFromPublicKey(publicKey)

        val isKeySame = account.isEqualsByKey(address, AuthorityType.ACTIVE)
        if (!isKeySame) {
            throw LocalException("Owner account checking exception")
        }
    }

    protected fun memoKey(name: String, password: String) =
        cryptoCoreComponent.getPrivateKey(name, password, AuthorityType.ACTIVE)

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
